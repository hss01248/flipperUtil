package com.hss01248.flipper.urlconnection;

import android.content.Context;
import android.util.Log;

import androidx.startup.Initializer;

import com.hss01248.aop.network.hook.OkhttpAspect;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

public class InitForUrlConnection implements Initializer<String>, OkhttpAspect.OkhttpHook {
    @Override
    public String create(Context context) {
        Log.d("init","InitForUrlConnection.init start");
        ProxyUrlConnectionUtil.proxyUrlConnection();
        OkhttpAspect.addHook(new InitForUrlConnection());
        return "InitForUrlConnection";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {

        return new ArrayList<>();
    }

    @Override
    public void beforeBuild(OkHttpClient.Builder builder) {
        List<Interceptor> interceptors1 = builder.interceptors();
        boolean isClientFromUrlConnection = false;
        boolean hasUrlTag = false;
        //todo 神策无法使用okhttp代理urlconnection,需要规避  : /debug?
        //com.sensorsdata.analytics.android.sdk.AnalyticsMessages.sendHttpRequest
        for (Interceptor interceptor : builder.networkInterceptors()) {
            if(interceptor.getClass().getName().contains("OkHttpURLConnection")){
                isClientFromUrlConnection = true;
                break;
                //okhttp3.internal.huc.OkHttpURLConnection.UnexpectedException#INTERCEPTOR
            }
        }
        //给urlconnection打标记: clientBuilder.interceptors().add(UnexpectedException.INTERCEPTOR);
        //okhttp3.internal.huc.OkHttpURLConnection.NetworkInterceptor
        for (Interceptor interceptor : interceptors1) {
            if(interceptor instanceof ProxyUrlInterceptor){
                hasUrlTag = true;
                break;
            }
        }
        if(isClientFromUrlConnection && !hasUrlTag){
            builder.addInterceptor(new ProxyUrlInterceptor());
        }
    }
}
