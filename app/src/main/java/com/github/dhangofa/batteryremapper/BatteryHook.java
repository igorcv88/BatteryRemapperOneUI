package com.igcv.batteryremapper;

import android.content.Intent;
import android.os.BatteryManager;

import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class BatteryHook implements IXposedHookLoadPackage {

    private static final AtomicBoolean HOOK_INSTALLED = new AtomicBoolean(false);

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

        // Apply visual battery remapping only inside SystemUI.
        if (!"com.android.systemui".equals(lpparam.packageName)) {
            return;
        }

        // Vector may invoke the legacy module entry point more than once for the
        // same SystemUI process. Registering the hook twice remaps the already
        // remapped value a second time (for example, physical 57 -> 66 -> 79).
        if (!HOOK_INSTALLED.compareAndSet(false, true)) {
            XposedBridge.log(
                    "BatteryRemapper: hook already installed, skipping."
            );
            return;
        }

        try {
            XposedHelpers.findAndHookMethod(
                    Intent.class,
                    "getIntExtra",
                    String.class,
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            String key = (String) param.args[0];

                            if (!BatteryManager.EXTRA_LEVEL.equals(key)) {
                                return;
                            }

                            // Only alter the actual sticky battery broadcast.
                            // This avoids touching unrelated SystemUI intents that
                            // happen to use an extra named "level".
                            Intent intent = (Intent) param.thisObject;
                            if (!Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                                return;
                            }

                            int originalLevel = (Integer) param.getResult();
                            int displayedLevel = remapBattery(originalLevel);

                            param.setResult(displayedLevel);
                        }
                    }
            );

            XposedBridge.log(
                    "BatteryRemapper: SystemUI hooked successfully."
            );
        } catch (Throwable t) {
            // Allow a later callback to retry if installation itself failed.
            HOOK_INSTALLED.set(false);
            XposedBridge.log(
                    "BatteryRemapper error: " + t
            );
        }
    }

    private int remapBattery(int physicalLevel) {
        int level = Math.max(0, Math.min(100, physicalLevel));

        // Physical 0–20% becomes displayed 0–10%.
        if (level <= 20) {
            return Math.round(level * 10f / 20f);
        }

        // Physical 80–100% becomes displayed 100%.
        if (level >= 80) {
            return 100;
        }

        // Physical 20–80% becomes displayed 10–100%.
        return 10 + Math.round((level - 20) * 90f / 60f);
    }
}
