package com.facebook.flipper.plugins.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import com.google.gson.Gson;
import com.hss01248.media.metadata.ExifUtil;
import com.hss01248.media.metadata.MetaDataUtil;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.Okio;

public class BodyUtil {
    public static Context context;
    public static final String HEADER_KEY_PATH = "meta-1-original-path";
    public static Map<String,String> getBodyDesc(Request request) {
        RequestBody requestBody = request.body();
        Map<String,String> meta = new TreeMap<>();
        if(requestBody == null){
            return meta;
        }
        boolean isRequestBodyText = false;
        MediaType mediaType = requestBody.contentType();
        String str = "";
        if (mediaType != null) {
             str = mediaType.toString();
            Log.v("chuck", "mediatype:" + str);
            if (str.contains("text") || str.contains("application/json")) {
                isRequestBodyText = true;
            }
        }

        if (!isRequestBodyText) {
            try {
                String path = request.url().toString();
                String originalFilePath = request.header(HEADER_KEY_PATH);
                if (!TextUtils.isEmpty(originalFilePath)
                        && new File(originalFilePath).exists()
                        && new File(originalFilePath).length() > 0) {
                    meta = MetaDataUtil.getMetaData(originalFilePath);
                } else {
                    /*File dir = new File(context.getExternalCacheDir(), "flipper-http-cache");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    String name = URLUtil.guessFileName(path, "", str);

                    //todo 文件拷贝
                    File tmp = new File(dir, name);
                    BufferedSink sink = Okio.buffer(Okio.sink(tmp));
                    requestBody.writeTo(sink);
                    sink.flush();
                    meta = MetaDataUtil.getMetaData(tmp.getAbsolutePath());
                    meta.put("00-tmp-path",tmp.getAbsolutePath());
                    tmp.delete();*/
                }


                return meta;
            } catch (Throwable throwable) {
                //throwable.printStackTrace();
            }

        }
        return meta;
    }

    public static Map getBodyMetaData(RequestBody requestBody){
        if(requestBody == null){
            return new HashMap();
        }
        String path = getFilePath(requestBody);
        if("MultipartBody".equals(path)){

           return getBodyMetaFromMultipart(requestBody);
        }else if(!TextUtils.isEmpty(path)){
            return MetaDataUtil.getMetaData(path);
        }
        return new HashMap();
    }

    public static Map<String,String> getBodyMetaDataAsStringMap(RequestBody requestBody){
        Map getBodyMetaData = getBodyMetaData(requestBody);
        if(getBodyMetaData == null){
            return new HashMap();
        }
        if(getBodyMetaData.isEmpty()){
            return getBodyMetaData;
        }
        Map<String,String> map =  new TreeMap<String,String>();
        for (Object key : getBodyMetaData.keySet()) {
            map.put(key+"",getBodyMetaData.get(key)+"");
        }
        return map;
    }

    private static Map getBodyMetaFromMultipart(RequestBody requestBody) {
        requestBody = getUnwrappedBody(requestBody);
        if(requestBody instanceof MultipartBody){
            MultipartBody body = (MultipartBody) requestBody;
            if(body.parts() == null){
                return null;
            }
            Map map = new TreeMap();
            for (int i = 0; i < body.parts().size(); i++) {
                MultipartBody.Part part = body.parts().get(i);
                Map mapCurrent = new TreeMap();
                map.put(i,mapCurrent);
                Headers headers = part.headers();
                if(headers != null){
                    mapCurrent.put("multi-headers", headers.toMultimap());
                }
                RequestBody body1 = part.body();
                Map mapPart = getBodyMetaData(body1);
                if(mapPart == null || mapPart.isEmpty()){
                    //key- value ,v为字符串
                    Buffer buffer =  new Buffer();
                    try {
                        body1.writeTo(buffer);
                        String s = buffer.readString(Charset.defaultCharset());
                        mapCurrent.put("value",s);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
                    //包含文件:
                    mapCurrent.put("value-file-meta",mapPart);
                }
            }
            return map;
        }
        return null;
    }

    public static String getFilePath(RequestBody requestBody) {
        try {
            if(requestBody == null){
                return "";
            }

            if(MediaType.parse("application/json").equals(requestBody.contentType())){
                return "";
            }
            //ContentTypeOverridingRequestBody
            //RequestBody$3

            requestBody = getUnwrappedBody(requestBody);
            Class clazz = requestBody.getClass();

            Log.d("fields", Arrays.toString(clazz.getDeclaredFields()));
            if(requestBody instanceof MultipartBody){
                return "MultipartBody";
            }

            String path = getPathFromRequestBody(requestBody);
            return path;

        } catch (Throwable throwable) {
            //throwable.printStackTrace();
        }
        return "";
    }

    private static RequestBody getUnwrappedBody(RequestBody requestBody) {
        Class clazz = requestBody.getClass();
        String name = clazz.getName();
        Log.d("class","body class:"+name);
        if (name.contains("ContentTypeOverridingRequestBody")) {
            Log.w("dd","ContentTypeOverridingRequestBody");
            try {
                Field field = clazz.getDeclaredField("delegate");
                field.setAccessible(true);
                requestBody = (RequestBody) field.get(requestBody);
                clazz = requestBody.getClass();
            }catch (Throwable throwable){
                throwable.printStackTrace();
            }
        }
        return requestBody;

    }

    private static String getPathFromRequestBody(RequestBody requestBody) {
        try {

            Class clazz = requestBody.getClass();
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                Object obj = declaredField.get(requestBody);
                Log.d("fields2",declaredField.getName()+":"+obj);
            }

            Field field = clazz.getDeclaredField("val$file");
            field.setAccessible(true);
            File file = (File) field.get(requestBody);
            if(file != null){
                String path =  file.getAbsolutePath();
                Log.d("fields3", "path: "+ path);
                return path;
            }
        }catch (NoSuchFieldException throwable){
            //throwable.printStackTrace();
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        return null;
    }

}
