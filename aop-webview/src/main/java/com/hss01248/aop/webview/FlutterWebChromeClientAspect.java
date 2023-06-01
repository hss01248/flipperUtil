package com.hss01248.aop.webview;

import android.net.Uri;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.hss01248.basewebview.dom.FileChooseImpl;
import com.hss01248.basewebview.dom.JsPermissionImpl;
import com.hss01248.logforaop.LogMethodAspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * by hss
 * data:2020/7/17
 * desc:
 */
@Aspect
public class FlutterWebChromeClientAspect {

    private static final String TAG = "WebChromeClientAspect";


    //public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams)
    @Around("execution(* io.flutter.plugins.webviewflutter.WebChromeClientHostApiImpl.WebChromeClientImpl.onShowFileChooser(..))")
    public Object onShowFileChooser(ProceedingJoinPoint joinPoint) throws Throwable {
        WebView webView = (WebView) joinPoint.getArgs()[0];
        ValueCallback<Uri[]> filePathCallback = (ValueCallback) joinPoint.getArgs()[1];
        WebChromeClient.FileChooserParams fileChooserParams = (WebChromeClient.FileChooserParams) joinPoint.getArgs()[2];

        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(JoinPoint joinPoin, String desc) {
                LogMethodAspect.IBefore.super.before(joinPoin, desc);
            }
        });

        return new FileChooseImpl().onShowFileChooser(webView, filePathCallback, fileChooserParams);
    }


    //public void onPermissionRequest(PermissionRequest request)
    @Around("execution(* io.flutter.plugins.webviewflutter.WebChromeClientHostApiImpl.WebChromeClientImpl.onPermissionRequest(..))")
    public void onPermissionRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        PermissionRequest request = (PermissionRequest) joinPoint.getArgs()[0];

        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(JoinPoint joinPoin, String desc) {
                LogMethodAspect.IBefore.super.before(joinPoin, desc);
            }
        });
         new JsPermissionImpl().onPermissionRequest(request);
    }

    //public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback)
    @Around("execution(* io.flutter.plugins.webviewflutter.WebChromeClientHostApiImpl.WebChromeClientImpl.onGeolocationPermissionsShowPrompt(..))")
    public void onGeolocationPermissionsShowPrompt(ProceedingJoinPoint joinPoint) throws Throwable {
        String origin = (String) joinPoint.getArgs()[0];
        GeolocationPermissions.Callback callback = (GeolocationPermissions.Callback) joinPoint.getArgs()[1];
        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(JoinPoint joinPoin, String desc) {
                LogMethodAspect.IBefore.super.before(joinPoin, desc);
            }
        });
        new JsPermissionImpl().onGeolocationPermissionsShowPrompt(origin,callback);
    }


}
