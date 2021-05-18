package com.hss01248.flipper;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface ConfigCallback {

    @Nullable
    Map<String, String> presetConfigMap();

    void onConfigSelected(@Nullable String config);
}
