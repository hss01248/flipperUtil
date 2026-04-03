package com.hss01248.flipper.http;

import com.facebook.flipper.plugins.network.FlipperExceptionInterceptor;
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.FlipperPerfEventListenerFactory;
import com.hss01248.aop.network.hook.OkhttpAspect;
import com.hss01248.flipper.FlipperUtil;
import com.hss01248.network.body.meta.interceptor.MyAppHelperInterceptor;

import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * @Despciption todo
 * @Author hss
 * @Date 30/06/2022 10:41
 * @Version 1.0
 */
public class OkhttpHookForFlipper implements OkhttpAspect.OkhttpHook{
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
           if(FlipperUtil.getNetworkFlipperPlugin() != null){
               builder.addNetworkInterceptor(new FlipperOkhttpInterceptor(FlipperUtil.getNetworkFlipperPlugin()));
           }
        }

       //头部添加flipper显示信息
        List<Interceptor> interceptors1 = builder.interceptors();
        boolean hasAppInterceptor = false;
        for (Interceptor interceptor : interceptors1) {
            if (interceptor instanceof MyAppHelperInterceptor) {
                hasAppInterceptor = true;
                break;
            }
        }
        if(!hasAppInterceptor){
            interceptors1.add(0,new MyAppHelperInterceptor());
        }

        // 注入异常捕获应用拦截器(最外层),捕获连接超时等 NetworkInterceptor 无法感知的异常
        if(FlipperUtil.getNetworkFlipperPlugin() != null){
            boolean hasExceptionInterceptor = false;
            for (Interceptor interceptor : interceptors1) {
                if (interceptor instanceof FlipperExceptionInterceptor) {
                    hasExceptionInterceptor = true;
                    break;
                }
            }
            if(!hasExceptionInterceptor){
                interceptors1.add(0, new FlipperExceptionInterceptor(FlipperUtil.getNetworkFlipperPlugin()));
            }
        }

        // 注入 EventListener (包装现有的 EventListener,不破坏原有逻辑)
        builder.eventListenerFactory(new FlipperPerfEventListenerFactory(builder));
    }

    @Override
    public int initOrder() {
        // OkhttpAspect runs higher initOrder first; go last so nothing overwrites eventListenerFactory.
        return -1000;
    }
}
