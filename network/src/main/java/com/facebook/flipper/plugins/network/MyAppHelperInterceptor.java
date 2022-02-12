package com.facebook.flipper.plugins.network;

import android.text.TextUtils;
import android.util.Log;

import com.facebook.flipper.plugins.network.BodyUtil;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class MyAppHelperInterceptor implements Interceptor {

    static final String KEY_FLIPPER_PREFIX = "flipper-";
    static final String KEY_REQUEST_ID = "meta-1-request-id";
    static Map<String, Request> requestMap = new HashMap<>();

    static Map<String, Map> requestBodyMap = new HashMap<>();

    static Map getRequestBodyMeta(Request request){
        String id = request.header(MyAppHelperInterceptor.KEY_REQUEST_ID);
        if(TextUtils.isEmpty(id) || !MyAppHelperInterceptor.requestBodyMap.containsKey(id)){
            return new HashMap();
        }
        Map<String,String> map = MyAppHelperInterceptor.requestBodyMap.get(id);
        if(map == null){
            return new HashMap();
        }
        return map;
    }
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        /*String path = BodyUtil.getFilePath(request.body());
        if(!TextUtils.isEmpty(path) ){
            try {
                request = request.newBuilder().header(BodyUtil.HEADER_KEY_PATH,path).build();
            }catch (Throwable throwable){
                request = request.newBuilder().header(BodyUtil.HEADER_KEY_PATH, URLEncoder.encode(path)).build();
            }
        }*/
        Map bodyMetaData = BodyUtil.getBodyMetaDataAsStringMap(request.body());
        String id = request.header(KEY_REQUEST_ID);
        if(TextUtils.isEmpty(id)){
            //应用层,放到第一个添加
            id = UUID.randomUUID().toString();
            request = request.newBuilder()
                    .header(KEY_REQUEST_ID, id)
                    .build();
            requestMap.put(id,request);
            requestBodyMap.put(id,bodyMetaData);
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

    public static Request removeDebugHeaders(Request request){
        Headers headers = request.headers();
        Set<String> names = headers.names();
        Headers.Builder builder = headers.newBuilder();
        for (String name : names) {
            if(name.startsWith(KEY_FLIPPER_PREFIX)){
                builder.removeAll(name);
            }else if(name.equals(KEY_REQUEST_ID) || BodyUtil.HEADER_KEY_PATH.equals(name)){
                builder.removeAll(name);
            }
        }
       return request.newBuilder().headers(builder.build()).build();
    }

    private void logResponseException(String id, Throwable throwable) {

    }

    private void logResponse(String id, Response response) {

    }

    private void logRequest(String id, Request request) {

    }
}
