package com.hss01248.network.chucker;


import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;

import com.blankj.utilcode.util.Utils;
import com.chuckerteam.chucker.api.ChuckerCollector;
import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.hss01248.aop.network.hook.OkhttpAspect;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * @Despciption todo
 * @Author hss
 * @Date 21/02/2022 11:46
 * @Version 1.0
 */
public class OkhttpHookForChucker implements OkhttpAspect.OkhttpHook, Initializer<String> {

    public static ChuckerCollector getChuckerCollector() {
        return chuckerCollector;
    }

    static ChuckerCollector chuckerCollector = new ChuckerCollector(Utils.getApp(),true);
    @Override
    public void beforeBuild(OkHttpClient.Builder builder) {
        List<Interceptor> interceptors1 = builder.interceptors();
        boolean hasChucker = false;
        for (Interceptor interceptor : interceptors1) {
             if(interceptor instanceof ChuckerInterceptor){
                hasChucker = true;
                 break;
            } else if(interceptor.getClass().getSimpleName().contains("Chuck")){
                 hasChucker = true;
                 break;
             }
        }

        if(!hasChucker){
            //无法使用:  interceptors1.add(0,new ChuckerInterceptor(U
            interceptors1.add(new ChuckerInterceptor(Utils.getApp(),chuckerCollector,512*1024));
            //在MyAppHelperInterceptor之后,所有其他用户添加的拦截器之前
        }
    }

    @Override
    public int initOrder() {
        //在OkhttpHookForFlipper之前
        return 0;
    }

    @Override
    public String create(Context context) {
        Log.d("init","OkhttpHookForChucker.init start");
        OkhttpAspect.addHook(new OkhttpHookForChucker());
        return "OkhttpHookForChucker";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}
