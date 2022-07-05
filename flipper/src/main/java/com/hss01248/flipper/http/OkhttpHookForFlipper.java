package com.hss01248.flipper.http;

import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
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
    }
}
