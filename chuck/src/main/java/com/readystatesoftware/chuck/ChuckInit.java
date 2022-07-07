package com.readystatesoftware.chuck;


import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;

import java.util.ArrayList;
import java.util.List;

/**
 * @Despciption todo
 * @Author hss
 * @Date 30/06/2022 10:04
 * @Version 1.0
 */
public class ChuckInit implements  Initializer<String> {




    @Override
    public String create(Context context) {
        Log.d("init","ChuckInit.init start");
        ChuckInterceptor.init((Application) context, true, new IChuckPwCheck() {
            @Override
            public void check(String input, Runnable success, Runnable fail) {

            }
        });
        return "ChuckInit";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {

        return new ArrayList<>();
    }
}
