package com.hss01248.flipper;

import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;

import com.blankj.utilcode.util.LogUtils;

import java.util.ArrayList;
import java.util.List;

public class InitForFlipper implements Initializer<String> {
    @Override
    public String create(Context context) {
        Log.d("init","FlipperUtil.init start");
        FlipperUtil.init(context,true,null);
        return "flipper";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        List<Class<? extends Initializer<?>>> list =  new ArrayList<>();
        try {
            list.add((Class<? extends Initializer<?>>) Class.forName("com.hss01248.flipper_leakcanary.LeakUtil"));
        } catch (Throwable e) {
            LogUtils.w(e);
        }
        return list;
    }
}
