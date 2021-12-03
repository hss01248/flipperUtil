package com.hss01248.flipper.aop.jsRNbridge;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import com.hss01248.logforaop.LogMethodAspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.util.Arrays;

import uk.co.alt236.webviewdebug.DebugWebChromeClient;
import uk.co.alt236.webviewdebug.DebugWebViewClient;

/**
 * @Despciption todo
 * @Author hss
 * @Date 03/12/2021 14:51
 * @Version 1.0
 */
@Aspect
public class WebviewClientAspect {
//android.webkit.WebView#setWebViewClient
    //android.webkit.WebView#setWebChromeClient

    @Around("execution(* android.webkit.WebView+.setWebViewClient(..))")
    public Object setWebViewClient(ProceedingJoinPoint joinPoint) throws Throwable{
        Object[] args = joinPoint.getArgs();
        if(args.length == 1 && args[0] instanceof WebViewClient){
            WebViewClient client = (WebViewClient) args[0];
            if(client instanceof DebugWebViewClient){
                Log.w("webaspect","已经是DebugWebViewClient");
               return joinPoint.proceed(args);

            }else {
                Log.i("webaspect","成功切入DebugWebViewClient");
                DebugWebViewClient debugWebViewClient = new DebugWebViewClient(client);
                args[0] = debugWebViewClient;
              return   joinPoint.proceed(args);
            }
        }else {
            Log.w("webaspect","webview client参数类型不对:"+ Arrays.toString(args));
        }
        return joinPoint.proceed(args);
    }

    @Around("execution(* android.webkit.WebView+.setWebChromeClient(..))")
    public Object addLog(ProceedingJoinPoint joinPoint) throws Throwable{
        Object[] args = joinPoint.getArgs();
        if(args.length == 1 && args[0] instanceof WebChromeClient){
            WebChromeClient client = (WebChromeClient) args[0];
            if(client instanceof DebugWebChromeClient){
                Log.w("webaspect","已经是DebugWebChromeClient");
                return joinPoint.proceed(args);

            }else {
                Log.i("webaspect","成功切入DebugWebChromeClient");
                DebugWebChromeClient debugWebViewClient = new DebugWebChromeClient(client);
                args[0] = debugWebViewClient;
                return   joinPoint.proceed(args);
            }
        }else {
            Log.w("webaspect","Chrome client参数类型不对:"+ Arrays.toString(args));
        }
        return joinPoint.proceed(args);
    }
}
