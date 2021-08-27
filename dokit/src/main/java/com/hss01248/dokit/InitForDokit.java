package com.hss01248.dokit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.startup.Initializer;

import java.util.ArrayList;
import java.util.List;

public class InitForDokit implements Initializer<String> {
    static Context context;
    @Override
    public String create(Context context) {
        InitForDokit.context = context;
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

    public static void goAppSettings(){
        Intent mIntent = new Intent();
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            mIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            mIntent.setAction(Intent.ACTION_VIEW);
            mIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
            mIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        InitForDokit.context.startActivity(mIntent);


    }
}
