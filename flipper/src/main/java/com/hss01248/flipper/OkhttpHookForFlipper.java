package com.hss01248.flipper;

import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.MyAppHelperInterceptor;

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
public class OkhttpHookForFlipper implements OkhttpAspect.OkhttpHook {
    @Override
    public void beforeBuild(OkHttpClient.Builder builder) {
        List<Interceptor> interceptors = builder.networkInterceptors();
        boolean hasFlipperPlugin = false;
        for (Interceptor interceptor : interceptors) {
            if(interceptor instanceof FlipperOkhttpInterceptor){
                hasFlipperPlugin = true;
                break;
            }
        }
        if(!hasFlipperPlugin){
            builder.addNetworkInterceptor(new FlipperOkhttpInterceptor(FlipperUtil.networkFlipperPlugin));
            //SslUtil.setAllCerPass(builder);
        }

        List<Interceptor> interceptors1 = builder.interceptors();
        boolean hasAppInterceptor = false;
        boolean hasChucker = false;
        boolean hasHttpLogging = false;
        for (Interceptor interceptor : interceptors1) {
            if(interceptor instanceof MyAppHelperInterceptor){
                hasAppInterceptor = true;
            }else if(interceptor instanceof ChuckerInterceptor){
                hasChucker = true;
            }else if(interceptor instanceof HttpLoggingInterceptor){
                hasHttpLogging = true;
            }else if(interceptor.getClass().getSimpleName().contains("LoggingInterceptor")){
                hasHttpLogging = true;
            }
        }
        if(!hasAppInterceptor){
            interceptors1.add(0,new MyAppHelperInterceptor());
        }
        if(!hasChucker){
            interceptors1.add(0,new ChuckerInterceptor(FlipperUtil.context));
        }
        if(!hasHttpLogging){
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
    }
}
