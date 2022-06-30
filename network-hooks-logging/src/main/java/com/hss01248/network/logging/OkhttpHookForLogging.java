package com.hss01248.network.logging;


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
public class OkhttpHookForLogging implements OkhttpAspect.OkhttpHook, Initializer<String> {
    @Override
    public void beforeBuild(OkHttpClient.Builder builder) {
        List<Interceptor> interceptors1 = builder.interceptors();
        boolean hasHttpLogging = false;
        for (Interceptor interceptor : interceptors1) {
            if(interceptor instanceof HttpLoggingInterceptor){
                hasHttpLogging = true;
                break;
            }else if(interceptor.getClass().getSimpleName().contains("LoggingInterceptor")){
                hasHttpLogging = true;
                break;
            }
        }

        if(!hasHttpLogging){
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
    }

    @Override
    public String create(Context context) {
        Log.d("init","OkhttpHookForLogging.init start");
        OkhttpAspect.addHook(new OkhttpHookForLogging());
        return "OkhttpHookForLogging";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return new ArrayList<>();
    }
}
