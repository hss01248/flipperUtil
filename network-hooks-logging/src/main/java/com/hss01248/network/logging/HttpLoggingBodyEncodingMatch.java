package com.hss01248.network.logging;

import android.text.TextUtils;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;

import okhttp3.Headers;

/**
 * 防止 HttpLoggingInterceptor.Level.BODY 时，retrofit 的 @Stream 注解失效。
 * <p>
 * 对应原 okhttp3.logging.HttpLoggingInterceptor 中 private static boolean bodyHasUnknownEncoding(Headers)。
 * 已由 AspectJ {@code HttpLogggingAspect} 改为 AndroidAOP {@link MatchClassMethod}。
 */
@AndroidAopMatchClassMethod(
        targetClassName = "okhttp3.logging.HttpLoggingInterceptor",
        methodName = {"bodyHasUnknownEncoding"},
        type = MatchType.SELF
)
class HttpLoggingBodyEncodingMatch implements MatchClassMethod {

    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        Headers headers = (Headers) joinPoint.getArgs()[0];
        String contentEncoding = headers.get("Content-Encoding");
        if (contentEncoding != null
                && (contentEncoding.equalsIgnoreCase("identity") || contentEncoding.equalsIgnoreCase("gzip"))) {
            return false;
        }
        String type = headers.get("Content-Type");
        if (!TextUtils.isEmpty(type)) {
            if (type.contains("event-stream")) {
                return true;
            }
            if (type.contains("text") || type.contains("json") || type.contains("xml") || type.contains("x-www-form-urlencoded")) {
                return false;
            }
        }
        String type2 = headers.get("flipper-body-type");
        if (!TextUtils.isEmpty(type2)) {
            if (type2.contains("text") || type2.contains("json") || type2.contains("xml") || type2.contains("x-www-form-urlencoded")) {
                return false;
            }
        }
        return true;
    }
}
