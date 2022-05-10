package com.hss01248.flipper;

import com.blankj.utilcode.util.LogUtils;
import com.chuckerteam.chucker.api.ChuckerInterceptor;
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.MyAppHelperInterceptor;
import com.facebook.flipper.plugins.network.ProxyUrlInterceptor;

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
        boolean isClientFromUrlConnection = false;
        boolean hasUrlTag = false;
        //LogUtils.d("networkInterceptors",builder.networkInterceptors());
        for (Interceptor interceptor : builder.networkInterceptors()) {
            if(interceptor.getClass().getName().contains("OkHttpURLConnection")){
                isClientFromUrlConnection = true;
                //okhttp3.internal.huc.OkHttpURLConnection.UnexpectedException#INTERCEPTOR
            }
        }
        //给urlconnection打标记: clientBuilder.interceptors().add(UnexpectedException.INTERCEPTOR);
        //okhttp3.internal.huc.OkHttpURLConnection.NetworkInterceptor
        for (Interceptor interceptor : interceptors1) {
            if(interceptor instanceof MyAppHelperInterceptor){
                hasAppInterceptor = true;
            }else if(interceptor instanceof ChuckerInterceptor){
                hasChucker = true;
            }else if(interceptor instanceof HttpLoggingInterceptor){
                hasHttpLogging = true;
            }else if(interceptor.getClass().getSimpleName().contains("LoggingInterceptor")){
                hasHttpLogging = true;
            }else  if(interceptor instanceof ProxyUrlInterceptor){
                hasUrlTag = true;
            }
        }
        if(!hasAppInterceptor){
            interceptors1.add(0,new MyAppHelperInterceptor());
        }
        if(!hasChucker){
            interceptors1.add(0,new ChuckerInterceptor(FlipperUtil.context));
        }
        //LogUtils.w("isClientFromUrlConnection ",isClientFromUrlConnection,"hasUrlTag",hasUrlTag);

        if(!hasHttpLogging){
            builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        if(isClientFromUrlConnection && !hasUrlTag){
            builder.addInterceptor(new ProxyUrlInterceptor());
        }

    }
}
