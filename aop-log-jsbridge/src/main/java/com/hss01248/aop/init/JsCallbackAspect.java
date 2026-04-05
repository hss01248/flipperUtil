package com.hss01248.aop.init;


import android.webkit.WebView;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hss01248.logforaop.LogMethodAspect;
import com.hss01248.logforaop.ObjParser;


/**
 * 预留：原 AspectJ 切点已注释；若恢复织入请改为 AndroidAOP MatchClassMethod。
 */
@Deprecated
public class JsCallbackAspect {

    public static final String TAG = "webAspect";
    static boolean enableLog = true;
    public static final String A = "execution(* webview.Html5Activity.runJavaScriptFunc(java.lang.String))";
    public static final String B = "execution(* webview.WebViewUtil.runJsFunc*(..))";
    public static final String C = "execution(* WebBridgeUtil.callback(..))";


    static Gson gson = new GsonBuilder().serializeNulls().create();

    public void addLog(ProceedJoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(enableLog, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {

                String des;
                try {
                    String s = joinPoint.getTargetMethod().getName() + "(..)";
                    des = s;
                    if (enableLog) {
                        Object[] args = joinPoint.getArgs();
                        if (args != null) {
                            des = s.replace("..", toStrings(joinPoint, args));
                        }
                        if (joinPoint.getTarget() != null) {
                            des = Integer.toHexString(joinPoint.getTarget().hashCode()) + "@" + des;
                        }
                    }
                    JsObjAspect.printLogToJsConsole(joinPoin, des);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    public static String toStrings(ProceedJoinPoint joinPoint, Object[] args) {
        StringBuilder sb = new StringBuilder();
        if (args == null) {
            return "";
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof WebView) {
                sb.append("webview").append(arg.hashCode());
            } else {
                if (i == 2) {
                    try {
                        JsObjAspect.printJsObjToJsConsole(joinPoint, gson.toJson(arg));
                    } catch (Throwable throwable) {
                        JsObjAspect.printLogToJsConsole(joinPoint, ObjParser.parseObj(arg));
                    }
                    sb.append("返回内容见上方");
                } else {
                    sb.append(toStr(arg));
                }
            }
            if (i != args.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private static String toStr(Object arg) {
        try {
            return gson.toJson(arg);
        } catch (Throwable throwable) {
            return ObjParser.parseObj(arg);
        }
    }
}
