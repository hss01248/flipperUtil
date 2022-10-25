package com.hss01248.network.body.meta.interceptor;

import android.app.Activity;
import android.text.TextUtils;

import com.blankj.utilcode.util.ActivityUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MyAppHelperInterceptor implements Interceptor {

    static final String KEY_FLIPPER_PREFIX = "flipper-";
    static final String KEY_REQUEST_ID = KEY_FLIPPER_PREFIX + "meta-1-request-id";

    static Map<String, Map> requestBodyMap = new HashMap<>();
    static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    public static String getRequestBodyMetaStr(Request request){
        Map map = getRequestBodyMeta(request);
        if(map == null || map.isEmpty()){
            return "";
        }
        return gson.toJson(map);

    }

    public static Map getRequestBodyMeta(Request request){
        String id = request.header(MyAppHelperInterceptor.KEY_REQUEST_ID);
        if(TextUtils.isEmpty(id) || !MyAppHelperInterceptor.requestBodyMap.containsKey(id)){
            return new HashMap();
        }
        Map map = MyAppHelperInterceptor.requestBodyMap.get(id);
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
        Map bodyMetaData = new HashMap();
        try {
            bodyMetaData =  BodyUtil.getBodyMetaData(request.body());
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }

        String id = request.header(KEY_REQUEST_ID);
        if(TextUtils.isEmpty(id)){
            //应用层,放到第一个添加
            //顶层activity
            String context = "background";
            Activity activity = ActivityUtils.getTopActivity();
            if(activity != null){
                context = activity.getClass().getSimpleName();
            }
            id = UUID.randomUUID().toString();

            String bodyType = "";
            RequestBody requestBody = request.body();
            if(requestBody != null){
                MediaType mediaType = requestBody.contentType();
                if(mediaType!=null){
                    bodyType = mediaType.toString();
                }
            }

            request = request.newBuilder()
                    .header(KEY_REQUEST_ID, id)
                    .header(KEY_FLIPPER_PREFIX+"body-type", bodyType)
                    .header(KEY_FLIPPER_PREFIX+"top-activity", context)
                    .build();
            requestBodyMap.put(id,bodyMetaData);
        }

        logRequest(id,request);
        try {
            Response response =  chain.proceed(request);
            logResponse(id,response);

            return response;
        }catch (Throwable throwable){
            logResponseException(id,throwable);
            //throw throwable;
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
       // throwable.printStackTrace();

    }

    private void logResponse(String id, Response response) {

    }

    private void logRequest(String id, Request request) {

    }
}
