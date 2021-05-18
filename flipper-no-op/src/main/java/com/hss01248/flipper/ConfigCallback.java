package com.hss01248.flipper;



import java.util.Map;

public interface ConfigCallback {


    Map<String, String> presetConfigMap();

    void onConfigSelected( String config);
}
