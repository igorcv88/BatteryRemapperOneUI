package com.igcv.batteryremapper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import io.github.libxposed.service.XposedService;

public class MainActivity extends Activity implements BatteryRemapperApp.ServiceListener {
    private RadioGroup fullLevelGroup;
    private TextView status;
    private boolean loading;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fullLevelGroup = findViewById(R.id.full_level_group);
        status = findViewById(R.id.framework_status);
        setChoicesEnabled(false);
        fullLevelGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (loading || checkedId == -1) return;
            XposedService service = ((BatteryRemapperApp) getApplication()).getService();
            if (service == null) return;

            int selected = checkedId == R.id.full_90 ? 90
                    : checkedId == R.id.full_85 ? 85 : 80;
            saveFullLevel(service, selected);
        });
    }

    @Override protected void onStart() {
        super.onStart();
        ((BatteryRemapperApp) getApplication()).addServiceListener(this);
    }

    @Override protected void onStop() {
        ((BatteryRemapperApp) getApplication()).removeServiceListener(this);
        super.onStop();
    }

    @Override public void onServiceChanged(XposedService service) {
        runOnUiThread(() -> bindServiceState(service));
    }

    private void saveFullLevel(XposedService service, int selected) {
        try {
            SharedPreferences preferences =
                    service.getRemotePreferences(BatteryHook.PREFERENCES_GROUP);
            boolean saved = preferences.edit()
                    .putInt(BatteryHook.KEY_FULL_LEVEL, selected)
                    .commit();
            Toast.makeText(this,
                    saved ? getString(R.string.saved, selected) : getString(R.string.save_failed),
                    Toast.LENGTH_SHORT).show();
        } catch (RuntimeException exception) {
            Toast.makeText(this, R.string.save_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void bindServiceState(XposedService service) {
        if (service == null) {
            status.setText(R.string.framework_unavailable);
            setChoicesEnabled(false);
            return;
        }

        try {
            if ((service.getFrameworkProperties() & XposedService.PROP_CAP_REMOTE) == 0) {
                status.setText(R.string.framework_remote_unavailable);
                setChoicesEnabled(false);
                return;
            }

            int current = BatteryMapping.sanitizeFullLevel(
                    service.getRemotePreferences(BatteryHook.PREFERENCES_GROUP)
                            .getInt(BatteryHook.KEY_FULL_LEVEL, BatteryMapping.DEFAULT_FULL_LEVEL));
            loading = true;
            fullLevelGroup.check(current == 90 ? R.id.full_90
                    : current == 85 ? R.id.full_85 : R.id.full_80);
            loading = false;
            setChoicesEnabled(true);
            status.setText(getString(R.string.framework_connected,
                    service.getFrameworkName(), service.getApiVersion()));
        } catch (RuntimeException exception) {
            loading = false;
            status.setText(R.string.framework_unavailable);
            setChoicesEnabled(false);
        }
    }

    private void setChoicesEnabled(boolean enabled) {
        fullLevelGroup.setEnabled(enabled);
        for (int index = 0; index < fullLevelGroup.getChildCount(); index++) {
            fullLevelGroup.getChildAt(index).setEnabled(enabled);
        }
    }
}
