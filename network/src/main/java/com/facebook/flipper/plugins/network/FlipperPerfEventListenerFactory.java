package com.facebook.flipper.plugins.network;

import java.lang.reflect.Field;

import okhttp3.Call;
import okhttp3.EventListener;
import okhttp3.OkHttpClient;

public class FlipperPerfEventListenerFactory implements EventListener.Factory {

    private EventListener.Factory originalFactory;

    public FlipperPerfEventListenerFactory(OkHttpClient.Builder builder) {
        try {
            Field field = OkHttpClient.Builder.class.getDeclaredField("eventListenerFactory");
            field.setAccessible(true);
            Object factory = field.get(builder);
            if (factory instanceof EventListener.Factory) {
                this.originalFactory = (EventListener.Factory) factory;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public EventListener create(Call call) {
        android.util.Log.d("FlipperPerf", "EventListener create called for call: " + call);
        EventListener original = null;
        if (originalFactory != null) {
            original = originalFactory.create(call);
        }
        return new FlipperPerfEventListener(original, call);
    }
}
