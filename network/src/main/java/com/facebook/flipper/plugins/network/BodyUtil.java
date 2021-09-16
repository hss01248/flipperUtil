package com.facebook.flipper.plugins.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import com.google.gson.Gson;
import com.hss01248.media.metadata.ExifUtil;
import com.hss01248.media.metadata.MetaDataUtil;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
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
                    File dir = new File(context.getExternalCacheDir(), "flipper-http-cache");
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    String name = URLUtil.guessFileName(path, "", str);
                    File tmp = new File(dir, name);
                    BufferedSink sink = Okio.buffer(Okio.sink(tmp));
                    requestBody.writeTo(sink);
                    sink.flush();
                    meta = MetaDataUtil.getMetaData(tmp.getAbsolutePath());
                    meta.put("00-tmp-path",tmp.getAbsolutePath());
                    tmp.delete();
                }


                return meta;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
        return meta;
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
            Class clazz = requestBody.getClass();
            String name = clazz.getName();
            Log.w("class","body class:"+name);
            if (name.contains("ContentTypeOverridingRequestBody")) {
                Field field = clazz.getDeclaredField("delegate");
                field.setAccessible(true);
                requestBody = (RequestBody) field.get(requestBody);

                clazz = requestBody.getClass();
            }
            Log.w("fields", Arrays.toString(clazz.getDeclaredFields()));
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                Object obj = declaredField.get(requestBody);
                Log.w("fields",declaredField.getName()+":"+obj);
            }
            Field field = clazz.getDeclaredField("val$file");
            field.setAccessible(true);
            File file = (File) field.get(requestBody);
            return file.getAbsolutePath();
        } catch (Throwable throwable) {
            //throwable.printStackTrace();
        }
        return "";
    }

}
