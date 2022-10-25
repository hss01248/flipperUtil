package com.hss01248.network.logging;

import android.text.TextUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import okhttp3.Headers;

/**
 * @Despciption todo
 * @Author hss
 * @Date 25/10/2022 14:08
 * @Version 1.0
 */
@Aspect
public class HttpLogggingAspect {

    /**
     * 防止HttpLoggingInterceptor.Level.BODY时,retrofit的@Stream注解失效
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("execution(* okhttp3.logging.HttpLoggingInterceptor.bodyHasUnknownEncoding(..))")
    public Object setWebViewClient(ProceedingJoinPoint joinPoint) throws Throwable{
        Headers headers = (Headers) joinPoint.getArgs()[0];
        //原始逻辑
        String contentEncoding = headers.get("Content-Encoding");
        if(contentEncoding != null
                && (contentEncoding.equalsIgnoreCase("identity") || contentEncoding.equalsIgnoreCase("gzip"))){
            return false;
        }
        //新增逻辑
        String type = headers.get("Content-Type");
        if(!TextUtils.isEmpty(type)){
            if(type.contains("text") || type.contains("json") || type.contains("xml") || type.contains("x-www-form-urlencoded")){
                return false;
            }
        }
        String type2 = headers.get("flipper-body-type");
        if(!TextUtils.isEmpty(type2)){
            if(type2.contains("text") || type2.contains("json") || type2.contains("xml") || type2.contains("x-www-form-urlencoded")){
                return false;
            }
        }
        return true;
    }


}
