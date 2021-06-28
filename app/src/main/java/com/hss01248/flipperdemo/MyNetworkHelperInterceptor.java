package com.hss01248.flipperdemo;

import android.text.TextUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class MyNetworkHelperInterceptor implements Interceptor {
    static final String KEY_REQUEST_ID = "helper-request-id";
    static Map<String, Request> requestMap = new HashMap<>();
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String id = request.header(KEY_REQUEST_ID);
        if(!TextUtils.isEmpty(id)){
            //应用层,放到第一个添加


        }
        return null;
    }
}
