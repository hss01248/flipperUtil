package com.hss01248.flipper_leakcanary;

import static com.hss01248.flipper_collector.FlipperCollector.addPlugin;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.facebook.flipper.plugins.leakcanary2.FlipperLeakListener;
import com.facebook.flipper.plugins.leakcanary2.LeakCanary2FlipperPlugin;

import java.util.ArrayList;
import java.util.List;

import leakcanary.LeakCanary;

/**
 * @Despciption todo
 * @Author hss
 * @Date 29/12/2022 14:55
 * @Version 1.0
 */
public class LeakUtil implements Initializer<String> {
    @NonNull
    @Override
    public String create(@NonNull Context context) {
        LeakCanary.Config config = new LeakCanary.Config.Builder(new LeakCanary.Config())
                //.eventListeners()
                .onHeapAnalyzedListener(new FlipperLeakListener()).build();
         LeakCanary.setConfig(config);
        addPlugin(new LeakCanary2FlipperPlugin());
        return "leakcanary";
    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}
