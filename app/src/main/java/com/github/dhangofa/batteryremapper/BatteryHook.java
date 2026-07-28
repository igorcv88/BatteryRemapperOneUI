package com.github.dhangofa.batteryremapper;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.os.Bundle;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class BatteryHook implements IXposedHookLoadPackage {
    
    // -1 = Neutral/Unknown, 0 = Force OFF, 1 = Force ON
    private static int appliedSaverState = -1;      

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        
        // ------------------------------------------------------------------
        // HOOK: SYSTEM UI - Visuals, Hysteresis Saver, & Shutdown Timer
        // ------------------------------------------------------------------
        if (!lpparam.packageName.equals("com.android.systemui")) return;

        try {
            XposedHelpers.findAndHookMethod(Intent.class, "getIntExtra", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[0];
                    
                    if (BatteryManager.EXTRA_LEVEL.equals(key)) {
                        int originalLevel = (Integer) param.getResult();
                        Intent intent = (Intent) param.thisObject;
                        Bundle extras = intent.getExtras();
                        
                        int plugged = (extras != null) ? extras.getInt(BatteryManager.EXTRA_PLUGGED, 0) : 0;
                        int displayedLevel = remapBattery(originalLevel);
                        Context context = AndroidAppHelper.currentApplication();

                        // 1. BATTERY SAVER HYSTERESIS LOGIC
                        if (context != null) {
                            handleBatterySaverLogic(context, displayedLevel, plugged);
                        }
                        
                        // 2. APPLY VISUAL SPOOF
                        param.setResult(displayedLevel);
                    }
                }
            });
            XposedBridge.log("BatteryRemapper: System UI Hooked Successfully.");
        } catch (Throwable t) {
            XposedBridge.log("BatteryRemapper Error: " + t.getMessage());
        }
    }

    private void handleBatterySaverLogic(Context context, int level, int plugged) {
        // CHARGING: Force OFF immediately
        if (plugged != 0) {
            if (appliedSaverState != 0) {
                setBatterySaver(context, false);
                appliedSaverState = 0;
            }
            return;
        }

        // UNPLUGGED: Hysteresis Logic
        if (level <= 20) {
            // Below 20%: Force ON
            if (appliedSaverState != 1) {
                setBatterySaver(context, true);
                appliedSaverState = 1;
            }
        } else if (level > 50) {
            // Above 50%: Force OFF
            if (appliedSaverState != 0) {
                setBatterySaver(context, false);
                appliedSaverState = 0;
            }
        }
        // Levels 21-50: Do nothing (Maintain state)
    }

    private void setBatterySaver(Context context, boolean enable) {
        try {
            // 1. Native API
            Object powerManager = context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null) {
                XposedHelpers.callMethod(powerManager, "setPowerSaveModeEnabled", enable);
            }
            // 2. ROM-Specific UI Authorized Fallback
            try {
                Class<?> saverUtils = XposedHelpers.findClass("com.android.settingslib.fuelgauge.BatterySaverUtils", context.getClassLoader());
                XposedHelpers.callStaticMethod(saverUtils, "setPowerSaveMode", context, enable, true);
            } catch (Throwable ignored) { }
            
            XposedBridge.log("BatteryRemapper: Battery Saver -> " + (enable ? "ON" : "OFF"));
        } catch (Throwable t) {
            XposedBridge.log("BatteryRemapper: Toggle Error: " + t.getMessage());
        }
    }

    private int remapBattery(int physicalLevel) {
        int level = Math.max(0, Math.min(100, physicalLevel));
        if (physicalLevel <= 20) {
            return Math.round(level*10f/20f);
        }
        if (physicalLevel >= 80) {
            return 100;
        }
        return 10 + Math.round((level - 20) * 90f / 60f);
    }
}
