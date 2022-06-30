package com.hss01248.aop.init;


import android.os.Build;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import com.hss01248.logforaop.LogMethodAspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.lang.reflect.Field;


@Aspect
public class JsObjAspect {

    public static final String TAG = "webAspect";
   public static boolean enableLog = true;
    public  static  IGetUrl getUrl;

    @Around("@annotation(android.webkit.JavascriptInterface)")
    public Object addLog(ProceedingJoinPoint joinPoint) throws Throwable{
        return LogMethodAspect.logAround(enableLog, TAG,false, joinPoint, new LogMethodAspect.IAround() {
            @Override
            public void before(ProceedingJoinPoint joinPoin,String desc) {
                //去注入的java对象里拿到webview引用,然后调用其打印log到console的方法:
                printLogToJsConsole(joinPoin,desc);
            }

            @Override
            public String descExtraForLog() {
                if(getUrl != null){
                      return "url:"+getUrl.getUrl();
                }
                return "please set JsObjAspect.getUrl";
            }
        });
    }

    public static void printLogToJsConsole(JoinPoint joinPoin, String desc) {
        WebView webView = getWebView(joinPoin);
        logByWebView(webView,desc);
    }

    public static void printJsObjToJsConsole(JoinPoint joinPoin, String desc) {
        WebView webView = getWebView(joinPoin);
        logObjByWebView(webView,desc);
    }

    private static WebView getWebView(JoinPoint joinPoin) {
        WebView webView = getFromThisFileds(joinPoin);
        if(webView == null){
            return getFromArgs(joinPoin);
        }
        return webView;
    }

    private static WebView getFromArgs(JoinPoint joinPoin) {
        Object[] args = joinPoin.getArgs();
        if(args == null || args.length == 0){
            return null;
        }
        for (Object arg : args) {
            if(arg instanceof WebView){
                if(arg != null){
                    return (WebView) arg;
                }
            }
        }
        return null;
    }

    private static WebView getFromThisFileds(JoinPoint joinPoin) {
        Object obj = joinPoin.getThis();
        if(obj == null){
            return null;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        if(fields == null || fields.length ==0){
            return null;
        }
        for (Field field : fields) {
            field.setAccessible(true);
            if(WebView.class.isAssignableFrom(field.getType())){
                try {
                    WebView webView = (WebView) field.get(obj);
                    if(webView != null){
                        return webView;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static void logByWebView(WebView webView,final String desc) {
        if(webView == null){
            return;
        }
        if(TextUtils.isEmpty(desc)){
            return;
        }
        webView.post(new Runnable() {
            @Override
            public void run() {
                String desc2 = desc;
                if(desc2.contains("'")){
                    desc2 = desc2.replaceAll("'","\"");
                }
                if(desc2.contains("\n")){
                    desc2 = desc2.replaceAll("\n"," ");
                }
                //desc内部有单引号会导致log方法识别到没有)
                String js = "javascript:console.log('"+desc2+"')";
                //Log.v(TAG,"log to js console:"+js);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                }else {
                    webView.loadUrl(js);
                }

            }
        });
    }

    private static void logObjByWebView(WebView webView,final String desc) {
        if(webView == null){
            return;
        }
        if(TextUtils.isEmpty(desc)){
            return;
        }
        webView.post(new Runnable() {
            @Override
            public void run() {
                String desc2 = desc;
                if(desc2.contains("'")){
                    desc2 = desc2.replaceAll("'","\"");
                }
                if(desc2.contains("\n")){
                    desc2 = desc2.replaceAll("\n"," ");
                }
                //desc内部有单引号会导致log方法识别到没有)
                String js = "javascript:console.log("+desc2+")";
                //Log.v(TAG,"log to js console:"+js);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(js, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                        }
                    });
                }else {
                    webView.loadUrl(js);
                }

            }
        });
    }

    public interface IGetUrl{
        String getUrl();
    }

}
