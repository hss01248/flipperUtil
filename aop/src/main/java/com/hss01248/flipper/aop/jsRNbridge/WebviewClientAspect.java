package com.hss01248.flipper.aop.jsRNbridge;

import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebViewClient;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;

import java.util.Arrays;

import uk.co.alt236.webviewdebug.DebugWebChromeClient;
import uk.co.alt236.webviewdebug.DebugWebViewClient;

/**
 * WebView 调试客户端织入（AndroidAOP）。
 */
public class WebviewClientAspect {

    public static Object interceptSetWebViewClient(ProceedJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length == 1 && args[0] instanceof WebViewClient) {
            WebViewClient client = (WebViewClient) args[0];
            if (client instanceof DebugWebViewClient) {
                Log.w("webaspect", "已经是DebugWebViewClient");
                return joinPoint.proceed(args);
            } else {
                Log.i("webaspect", "成功切入DebugWebViewClient");
                DebugWebViewClient debugWebViewClient = new DebugWebViewClient(client);
                args[0] = debugWebViewClient;
                return joinPoint.proceed(args);
            }
        } else {
            Log.w("webaspect", "webview client参数类型不对:" + Arrays.toString(args));
        }
        return joinPoint.proceed(args);
    }

    public static Object interceptSetWebChromeClient(ProceedJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        if (args.length == 1 && args[0] instanceof WebChromeClient) {
            WebChromeClient client = (WebChromeClient) args[0];
            if (client instanceof DebugWebChromeClient) {
                Log.w("webaspect", "已经是DebugWebChromeClient");
                return joinPoint.proceed(args);
            } else {
                Log.i("webaspect", "成功切入DebugWebChromeClient");
                DebugWebChromeClient debugWebViewClient = new DebugWebChromeClient(client);
                args[0] = debugWebViewClient;
                return joinPoint.proceed(args);
            }
        } else {
            Log.w("webaspect", "Chrome client参数类型不对:" + Arrays.toString(args));
        }
        return joinPoint.proceed(args);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "android.webkit.WebView",
        methodName = {"setWebViewClient"},
        type = MatchType.EXTENDS
)
class WebViewSetWebViewClientMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return WebviewClientAspect.interceptSetWebViewClient(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "android.webkit.WebView",
        methodName = {"setWebChromeClient"},
        type = MatchType.EXTENDS
)
class WebViewSetWebChromeClientMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return WebviewClientAspect.interceptSetWebChromeClient(joinPoint);
    }
}
