package com.hss01248.oom;


import android.app.Application;
import android.content.Context;


import androidx.startup.Initializer;

import java.util.ArrayList;
import java.util.List;

public class InitForOOM implements Initializer<String> {
   public static Context context;
    @Override
    public String create(Context context) {
        if(context instanceof Application){
            LeakDetector.init((Application) context);
        }
        return "Dokit";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }


}
