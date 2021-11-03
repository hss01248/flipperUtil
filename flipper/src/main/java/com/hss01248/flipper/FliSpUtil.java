package com.hss01248.flipper;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by huangshuisheng on 2017/12/13.
 */

 class FliSpUtil {



    private static Context context;

    public static void init(Context app) {
        context = app;
    }

    private static final String SP_FILE_NAME = "SpUtilAD";


    public static void putLong(String key,long val){
        getSP().edit().putLong(key,val)
            .apply();
    }

    public static long getLong(String key, long defVal) {
        return getSP().getLong(key, defVal);
    }

    public static void putBoolean(String key, boolean val) {
        getSP().edit().putBoolean(key, val).apply();
    }
    public static boolean putBooleanNow(String key, boolean val) {
        return getSP().edit().putBoolean(key, val).commit();
    }

    public static boolean getBoolean(String key, boolean defVal) {
        return getSP().getBoolean(key, defVal);
    }

    public static void putInt(String key, int val) {
        getSP().edit().putInt(key, val).apply();
    }

    public static boolean putIntNow(String key, int val) {
       return getSP().edit().putInt(key, val).commit();
    }

    public static int getInt(String key, int defVal) {
        return getSP().getInt(key, defVal);
    }

    public static void putString(String key, String val) {
        getSP().edit().putString(key, val).apply();
    }

    public static String getString(String key, String defVal) {
        return getSP().getString(key, defVal);
    }

    public static void putFloat(String key, float val) {
        getSP().edit().putFloat(key, val).apply();
    }

    public static float getFloat(String key, float defVal) {
        return getSP().getFloat(key, defVal);
    }

    private static SharedPreferences getSP() {
        return context.getSharedPreferences(SP_FILE_NAME, Context.MODE_PRIVATE);
    }
}
