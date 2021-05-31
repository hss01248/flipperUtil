package com.hss01248.flipper;

import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;

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
        return new ArrayList<>();
    }
}
