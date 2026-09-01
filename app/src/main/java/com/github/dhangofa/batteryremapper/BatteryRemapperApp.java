package com.igcv.batteryremapper;

import android.app.Application;
import androidx.annotation.NonNull;
import java.util.concurrent.CopyOnWriteArraySet;
import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

public class BatteryRemapperApp extends Application
        implements XposedServiceHelper.OnServiceListener {
    interface ServiceListener { void onServiceChanged(XposedService service); }

    private final CopyOnWriteArraySet<ServiceListener> listeners = new CopyOnWriteArraySet<>();
    private volatile XposedService service;

    @Override public void onCreate() {
        super.onCreate();
        XposedServiceHelper.registerListener(this);
    }
    @Override public void onServiceBind(@NonNull XposedService boundService) {
        service = boundService;
        notifyListeners(boundService);
    }
    @Override public void onServiceDied(@NonNull XposedService deadService) {
        if (service == deadService) {
            service = null;
            notifyListeners(null);
        }
    }
    void addServiceListener(ServiceListener listener) {
        listeners.add(listener);
        listener.onServiceChanged(service);
    }
    void removeServiceListener(ServiceListener listener) { listeners.remove(listener); }
    XposedService getService() { return service; }
    private void notifyListeners(XposedService currentService) {
        for (ServiceListener listener : listeners) listener.onServiceChanged(currentService);
    }
}
