package com.hss01248.http.ssl.ignore;


import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;

import com.hss01248.aop.network.hook.OkhttpAspect;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * @Despciption todo
 * @Author hss
 * @Date 30/06/2022 10:04
 * @Version 1.0
 */
public class IgnoreOkhttpCerCheckHook implements OkhttpAspect.OkhttpHook , Initializer<String> {

    public static boolean shouldIgnore = true;
    @Override
    public void beforeBuild(OkHttpClient.Builder builder) {
        if(shouldIgnore){
            SslUtil.setAllCerPass(builder);
        }
    }

    @Override
    public String create(Context context) {
        Log.d("init","IgnoreOkhttpCerCheckHook.init start");
        OkhttpAspect.addHook(new IgnoreOkhttpCerCheckHook());
        return "IgnoreOkhttpCerCheckHook";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {

        return new ArrayList<>();
    }
}
