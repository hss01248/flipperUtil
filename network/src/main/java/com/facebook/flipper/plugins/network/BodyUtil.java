package com.facebook.flipper.plugins.network;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import com.hss01248.media.metadata.ExifUtil;

import java.io.File;
import java.lang.reflect.Field;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.Okio;

public class BodyUtil {
    public static Context context;
    public static String getBodyDesc(Request request) {
        RequestBody requestBody = request.body();

        boolean isRequestBodyImg = false;
        MediaType mediaType = requestBody.contentType();
        if (mediaType != null) {
            String str = mediaType.toString();
            Log.v("chuck", "mediatype:" + str);
            if (str.contains("image")) {
                isRequestBodyImg = true;
            }
        }
        if (isRequestBodyImg) {
            try {
                File dir = new File(context.getExternalCacheDir(), "chuckimgcache");
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                String path = request.url().toString();
                String name = URLUtil.guessFileName(path, "", "image/*");
                File tmp = new File(dir, name);
                BufferedSink sink = Okio.buffer(Okio.sink(tmp));
                requestBody.writeTo(sink);
                sink.flush();
                String originalFilePath = getFilePath(requestBody);
                String exif = "";
                if (!TextUtils.isEmpty(originalFilePath) && new File(originalFilePath).exists() && new File(originalFilePath).length() > 0) {
                    exif = ExifUtil.getExifStr(originalFilePath);
                } else {
                    exif = ExifUtil.getExifStr(tmp.getAbsolutePath());
                }
                exif = tmp.getAbsolutePath() + "\n" + exif;
                return exif;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }

        }
        return null;
    }

    private static String getFilePath(RequestBody requestBody) {
        try {
            //ContentTypeOverridingRequestBody
            //RequestBody$3
            Class clazz = requestBody.getClass();
            String name = clazz.getName();
            if (name.contains("ContentTypeOverridingRequestBody")) {
                Field field = clazz.getDeclaredField("delegate");
                field.setAccessible(true);
                requestBody = (RequestBody) field.get(requestBody);

                clazz = requestBody.getClass();
            }

            Field field = clazz.getDeclaredField("val$file");
            field.setAccessible(true);
            File file = (File) field.get(requestBody);
            return file.getAbsolutePath();


        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return "";
    }

}
