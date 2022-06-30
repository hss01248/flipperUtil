package com.hss01248.aop.init;


import android.webkit.WebView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hss01248.logforaop.LogMethodAspect;
import com.hss01248.logforaop.ObjParser;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;



//@Aspect
public class JsCallbackAspect {

    public static final String TAG = "webAspect";
    static boolean enableLog = true;
    public static final String A = "execution(* webview.Html5Activity.runJavaScriptFunc(java.lang.String))";
    public static final String B = "execution(* webview.WebViewUtil.runJsFunc*(..))";
    public static final String C = "execution(* WebBridgeUtil.callback(..))";



    static Gson gson = new GsonBuilder().serializeNulls().create();

    @Before(A + " || " +B+ " || " +C)
    public void addLog(JoinPoint joinPoint) throws Throwable{
        LogMethodAspect.logBefore(enableLog, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(JoinPoint joinPoin, String desc) {

                String des = "";
                //before
                try {
                    String s = joinPoint.getSignature().toShortString();
                    //Html5JsObj.handleBackBehavior(..)
                    des = s;
                    if(enableLog){
                        Object[] args = joinPoint.getArgs();
                        if(args!= null){
                            des = s.replace("..", toStrings(joinPoint,args));
                        }
                        if(joinPoint.getThis() != null){
                            des = Integer.toHexString(joinPoint.getThis().hashCode())+"@"+des;
                        }
                    }
                    JsObjAspect.printLogToJsConsole(joinPoin,des);
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });
    }

    public static String toStrings(JoinPoint joinPoint,Object[] args) {
        StringBuilder sb = new StringBuilder();
        if(args == null){
            return "";
        }
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if(arg instanceof WebView){
                sb.append("webview").append(arg.hashCode());
            }else {
                if(i == 2){
                    try {
                        JsObjAspect.printJsObjToJsConsole(joinPoint, gson.toJson(arg));
                    }catch (Throwable throwable){
                        JsObjAspect.printLogToJsConsole(joinPoint, ObjParser.parseObj(arg));
                    }
                    sb.append("返回内容见上方");
                }else {
                    sb.append(toStr(arg));
                }

               // sb.append(ObjParser.parseObj(arg));
            }
            if(i != args.length-1){
                sb.append(",");
            }
        }
        return sb.toString();
    }

    private static String toStr(Object arg) {
        try {
            return gson.toJson(arg);
        }catch (Throwable throwable){
            return ObjParser.parseObj(arg);
        }
    }
}
