package com.hss01248.flipper_collector;

import com.facebook.flipper.core.FlipperPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @Despciption todo
 * @Author hss
 * @Date 29/12/2022 14:46
 * @Version 1.0
 */
public class FlipperCollector {

    public static List<FlipperPlugin> getPlugins() {
        return plugins;
    }

    private static List<FlipperPlugin> plugins = new ArrayList<>();

    public static void addPlugin(FlipperPlugin plugin){
        plugins.add(plugin);
    }
}
