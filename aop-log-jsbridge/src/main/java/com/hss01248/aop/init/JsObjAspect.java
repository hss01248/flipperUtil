package com.hss01248.aop.init;


import android.os.Build;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;

import java.lang.reflect.Field;


/**
 * Js 桥接日志辅助；切点见 {@link AopJavascriptInterface} + {@link JsObjCut}。
 */
public class JsObjAspect {

    public static final String TAG = "webAspect";
    public static boolean enableLog = true;
    public static IGetUrl getUrl;

    public static void printLogToJsConsole(ProceedJoinPoint joinPoin, String desc) {
        WebView webView = getWebView(joinPoin);
        logByWebView(webView, desc);
    }

    public static void printJsObjToJsConsole(ProceedJoinPoint joinPoin, String desc) {
        WebView webView = getWebView(joinPoin);
        logObjByWebView(webView, desc);
    }

    private static WebView getWebView(ProceedJoinPoint joinPoin) {
        WebView webView = getFromThisFileds(joinPoin);
        if (webView == null) {
            return getFromArgs(joinPoin);
        }
        return webView;
    }

    private static WebView getFromArgs(ProceedJoinPoint joinPoin) {
        Object[] args = joinPoin.getArgs();
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof WebView) {
                return (WebView) arg;
            }
        }
        return null;
    }

    private static WebView getFromThisFileds(ProceedJoinPoint joinPoin) {
        Object obj = joinPoin.getTarget();
        if (obj == null) {
            return null;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        if (fields == null || fields.length == 0) {
            return null;
        }
        for (Field field : fields) {
            field.setAccessible(true);
            if (WebView.class.isAssignableFrom(field.getType())) {
                try {
                    WebView webView = (WebView) field.get(obj);
                    if (webView != null) {
                        return webView;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static void logByWebView(WebView webView, final String desc) {
        if (webView == null) {
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            return;
        }
        webView.post(new Runnable() {
            @Override
            public void run() {
                String desc2 = desc;
                if (desc2.contains("'")) {
                    desc2 = desc2.replaceAll("'", "\"");
                }
                if (desc2.contains("\n")) {
                    desc2 = desc2.replaceAll("\n", " ");
                }
                String js = "javascript:console.log('" + desc2 + "')";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                } else {
                    webView.loadUrl(js);
                }

            }
        });
    }

    private static void logObjByWebView(WebView webView, final String desc) {
        if (webView == null) {
            return;
        }
        if (TextUtils.isEmpty(desc)) {
            return;
        }
        webView.post(new Runnable() {
            @Override
            public void run() {
                String desc2 = desc;
                if (desc2.contains("'")) {
                    desc2 = desc2.replaceAll("'", "\"");
                }
                if (desc2.contains("\n")) {
                    desc2 = desc2.replaceAll("\n", " ");
                }
                String js = "javascript:console.log(" + desc2 + ")";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                } else {
                    webView.loadUrl(js);
                }

            }
        });
    }

    public interface IGetUrl {
        String getUrl();
    }

}
