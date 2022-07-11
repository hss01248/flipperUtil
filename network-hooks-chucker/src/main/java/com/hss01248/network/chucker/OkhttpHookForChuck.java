package com.hss01248.network.chucker;


import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;

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
public class OkhttpHookForChuck implements OkhttpAspect.OkhttpHook, Initializer<String> {
    @Override
    public void beforeBuild(OkHttpClient.Builder builder) {
        List<Interceptor> interceptors1 = builder.interceptors();
        boolean hasChucker = false;
        for (Interceptor interceptor : interceptors1) {
             /*if(interceptor instanceof ChuckInterceptor){
                hasChucker = true;
                 break;
            } else if(interceptor.getClass().getSimpleName().contains("Chuck")){
                 hasChucker = true;
                 break;
             }*/
        }

        if(!hasChucker){
           // builder.addInterceptor(new ChuckInterceptor(Utils.getApp()));
           // builder.addInterceptor(new PrintCookieNetworkInterceptor());
        }
    }

    @Override
    public String create(Context context) {
        Log.d("init","OkhttpHookForChucker.init start");
        OkhttpAspect.addHook(new OkhttpHookForChuck());
        return "OkhttpHookForChucker";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}
