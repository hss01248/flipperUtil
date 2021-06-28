package com.hss01248.flipperdemo;

import android.text.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class MyAppHelperInterceptor implements Interceptor {
    static final String KEY_REQUEST_ID = "helper-request-id";
    static Map<String, Request> requestMap = new HashMap<>();
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String id = request.header(KEY_REQUEST_ID);
        if(TextUtils.isEmpty(id)){
            //应用层,放到第一个添加
            request = request.newBuilder()
                    .header(KEY_REQUEST_ID, UUID.randomUUID().toString())
                    .build();
            requestMap.put(id,request);
        }
        logRequest(id,request);
        try {
            Response response =  chain.proceed(request);
            logResponse(id,response);

            return response;
        }catch (Throwable throwable){
            logResponseException(id,throwable);
            if(throwable instanceof IOException){
                throw throwable;
            }
            throw new IOException(throwable);
        }

    }

    private void logResponseException(String id, Throwable throwable) {

    }

    private void logResponse(String id, Response response) {

    }

    private void logRequest(String id, Request request) {

    }
}
