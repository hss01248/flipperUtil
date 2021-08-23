package com.hss01248.dokit;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;

import java.util.ArrayList;
import java.util.List;

public class InitForDokit implements Initializer<String> {
    @Override
    public String create(Context context) {
        Log.d("init","Dokit.init start");
        if(context instanceof Application){
            MyDokit.init((Application) context);
        }
        return "Dokit";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}
