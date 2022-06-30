package com.hss01248.flipper.urlconnection;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @Despciption todo
 * @Author hss
 * @Date 10/05/2022 12:11
 * @Version 1.0
 */
public class ProxyUrlInterceptor implements Interceptor {
    static final String KEY_FLIPPER_PREFIX = "flipper-";
    @Override
    public Response intercept(Chain chain) throws IOException {
        return chain.proceed(chain.request().newBuilder()
                //.url(chain.request().url().toString()+"?fromUlConnection=1")
                .header(KEY_FLIPPER_PREFIX+"fromUrlConnection","1")
                .build());
    }
}
