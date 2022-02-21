package com.hss01248.dokit;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.startup.Initializer;

public interface IDokitConfig {

    void loadUrl(Context context, String url);

    void report(Object o);

    default void goTestPage(){

    }

    default String getExtraInitClassName(){
        return "";

    }
}
