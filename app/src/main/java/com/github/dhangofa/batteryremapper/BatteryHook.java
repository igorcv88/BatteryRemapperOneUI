package com.igcv.batteryremapper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.util.Log;
import androidx.annotation.NonNull;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import io.github.libxposed.api.XposedModule;

public class BatteryHook extends XposedModule {
    static final String PREFERENCES_GROUP = "battery_mapping";
    static final String KEY_FULL_LEVEL = "full_level";
    private static final String SYSTEM_UI = "com.android.systemui";
    private static final String TAG = "BatteryRemapper";

    private final AtomicBoolean hookInstalled = new AtomicBoolean(false);
    private final AtomicInteger fullLevel = new AtomicInteger(BatteryMapping.DEFAULT_FULL_LEVEL);
    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceListener =
            (prefs, key) -> {
                if (KEY_FULL_LEVEL.equals(key)) updateFullLevel();
            };
    private SharedPreferences preferences;

    @Override
    public void onPackageReady(@NonNull PackageReadyParam param) {
        if (!SYSTEM_UI.equals(param.getPackageName()) || !param.isFirstPackage()) return;
        if (!hookInstalled.compareAndSet(false, true)) return;

        initializePreferences();

        try {
            Method getIntExtra = Intent.class.getDeclaredMethod(
                    "getIntExtra", String.class, int.class);
            hook(getIntExtra).intercept(chain -> {
                Object result = chain.proceed();
                if (!(result instanceof Integer)
                        || !BatteryManager.EXTRA_LEVEL.equals(chain.getArg(0))) return result;

                Intent intent = (Intent) chain.getThisObject();
                if (!Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) return result;
                return BatteryMapping.remap((Integer) result, fullLevel.get());
            });

            log(Log.INFO, TAG, "SystemUI hook installed; full level=" + fullLevel.get());
        } catch (Throwable throwable) {
            hookInstalled.set(false);
            log(Log.ERROR, TAG, "Unable to install SystemUI hook", throwable);
        }
    }

    private void initializePreferences() {
        try {
            preferences = getRemotePreferences(PREFERENCES_GROUP);
            updateFullLevel();
            preferences.registerOnSharedPreferenceChangeListener(preferenceListener);
        } catch (Throwable throwable) {
            preferences = null;
            fullLevel.set(BatteryMapping.DEFAULT_FULL_LEVEL);
            log(Log.WARN, TAG,
                    "Remote preferences unavailable; using default full level="
                            + BatteryMapping.DEFAULT_FULL_LEVEL,
                    throwable);
        }
    }

    private void updateFullLevel() {
        if (preferences == null) return;
        int stored = preferences.getInt(KEY_FULL_LEVEL, BatteryMapping.DEFAULT_FULL_LEVEL);
        int sanitized = BatteryMapping.sanitizeFullLevel(stored);
        fullLevel.set(sanitized);
        log(Log.INFO, TAG, "Full level changed to " + sanitized);
    }
}
