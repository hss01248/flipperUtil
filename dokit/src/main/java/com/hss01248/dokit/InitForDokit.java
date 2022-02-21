package com.hss01248.dokit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.startup.Initializer;

import com.didichuxing.doraemonkit.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class InitForDokit implements Initializer<String> {
   public static Context context;
    @Override
    public String create(Context context) {
        InitForDokit.context = context;
        Log.d("init","Dokit.init start");

        if(context instanceof Application){
            Utils.init((Application) context);
            MyDokit.init((Application) context);
        }
        return "Dokit";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
       String initClassName =  Utils.getApp().getSharedPreferences("dokit",Context.MODE_PRIVATE).getString("deps","");

        if(TextUtils.isEmpty(initClassName)){
           return new ArrayList<>();
        }
        try {
            Class clazz = Class.forName(initClassName);
            if(Initializer.class.isAssignableFrom(clazz)){
                ArrayList<Class<? extends Initializer<?>>> objects = new ArrayList<>();
                objects.add(clazz);
                return objects;
            }else {
               // Log.w("init",initClassName +"不是Initializer的子类");
            }
        } catch (Exception e) {
           // e.printStackTrace();
        }
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

    static void setExtraInitClassName(String initClassName){
        if(TextUtils.isEmpty(initClassName)){
            return;
        }
        try {
            Class clazz = Class.forName(initClassName);
            if(Initializer.class.isAssignableFrom(clazz)){
                Utils.getApp().getSharedPreferences("dokit",Context.MODE_PRIVATE).edit().putString("deps",initClassName).apply();
            }else {
                Log.w("init",initClassName +"不是Initializer的子类");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
