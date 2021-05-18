package com.hss01248.flipperdemo;

import android.app.Application;

import com.hss01248.flipper.ConfigCallback;
import com.hss01248.flipper.FlipperUtil;



import java.util.Map;

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlipperUtil.init(this, true, new ConfigCallback() {

            @Override
            public Map<String, String> presetConfigMap() {
                return null;
            }

            @Override
            public void onConfigSelected( String config) {

            }
        });
    }
}
