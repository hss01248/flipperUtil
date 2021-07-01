package com.hss01248.flipper;

import android.app.Application;
import android.content.Context;


import com.facebook.flipper.plugins.network.RequestBodyParser;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;

/**
 * 内部使用的okhttp是4.9.0, 如果需要使用3.12.x,请强制指定版本
 * configurations {
 *     all*.exclude group: 'xxxxx'
 *     all {
 *         resolutionStrategy {
 *             force "com.squareup.okhttp3:okhttp:3.12.1"
 *         }
 *     }
 * }
 */
public class FlipperUtil {

    public static void addInterceptor(OkHttpClient.Builder builder){

    }

    public static Interceptor getInterceptor(){
        return null;
    }

    public static void addConfigBox(Context context,ConfigCallback callback){

    }

    public static void setRequestBodyParser(RequestBodyParser requestBodyParser) {

    }


     static void init(Context app, boolean enable, ConfigCallback callback){

    }

}
