package com.hss01248.network.auth;


import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;

import com.hss01248.aop.network.hook.OkhttpAspect;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @Despciption todo
 * @Author hss
 * @Date 21/02/2022 11:46
 * @Version 1.0
 */
public class OkhttpHookForRemoveRange implements OkhttpAspect.OkhttpHook, Initializer<String> {
    @Override
    public void beforeBuild(OkHttpClient.Builder builder) {
        List<Interceptor> interceptors1 = builder.networkInterceptors();
        boolean hasHttpLogging = false;
        for (Interceptor interceptor : interceptors1) {
            if(interceptor instanceof HttpLoggingInterceptor){
                hasHttpLogging = true;
                break;
            }else if(interceptor.getClass().getSimpleName().contains("RemoveRangeInterceptor")){
                hasHttpLogging = true;
                break;
            }
        }

        if(!hasHttpLogging){
            interceptors1.add(new RemoveRangeInterceptor());
        }
    }

    @Override
    public String create(Context context) {
        Log.d("init","RemoveRangeInterceptor.init start");
        OkhttpAspect.addHook(new OkhttpHookForRemoveRange());
        return "RemoveRangeInterceptor";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}