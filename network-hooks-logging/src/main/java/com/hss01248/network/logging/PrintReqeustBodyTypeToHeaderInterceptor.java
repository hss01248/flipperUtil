package com.hss01248.network.logging;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @Despciption todo
 * @Author hss
 * @Date 25/10/2022 15:16
 * @Version 1.0
 */
@Deprecated
public class PrintReqeustBodyTypeToHeaderInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RequestBody requestBody = request.body();
        if(requestBody != null){
            if (requestBody.contentType() != null) {
                MediaType mediaType = requestBody.contentType();
                String type = mediaType.toString();
                Request build = request.newBuilder().addHeader("flipper-body-type", type).build();
                return chain.proceed(build);
            }
        }
        return chain.proceed(request);
    }
}
