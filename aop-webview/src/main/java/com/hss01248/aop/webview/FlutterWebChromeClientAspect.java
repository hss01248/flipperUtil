package com.hss01248.aop.webview;

import android.net.Uri;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;
import com.hss01248.basewebview.dom.FileChooseImpl;
import com.hss01248.basewebview.dom.JsPermissionImpl;
import com.hss01248.logforaop.LogMethodAspect;

/**
 * Flutter WebView ChromeClient 织入（AndroidAOP）。
 */
public class FlutterWebChromeClientAspect {

    private static final String TAG = "WebChromeClientAspect";

    public static Object interceptOnShowFileChooser(ProceedJoinPoint joinPoint) throws Throwable {
        WebView webView = (WebView) joinPoint.getArgs()[0];
        ValueCallback<Uri[]> filePathCallback = (ValueCallback) joinPoint.getArgs()[1];
        WebChromeClient.FileChooserParams fileChooserParams = (WebChromeClient.FileChooserParams) joinPoint.getArgs()[2];

        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {
            }
        });

        return new FileChooseImpl().onShowFileChooser(webView, filePathCallback, fileChooserParams);
    }

    public static void interceptOnPermissionRequest(ProceedJoinPoint joinPoint) throws Throwable {
        PermissionRequest request = (PermissionRequest) joinPoint.getArgs()[0];

        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {
            }
        });
        new JsPermissionImpl().onPermissionRequest(request);
    }

    public static void interceptOnGeolocationPermissionsShowPrompt(ProceedJoinPoint joinPoint) throws Throwable {
        String origin = (String) joinPoint.getArgs()[0];
        GeolocationPermissions.Callback callback = (GeolocationPermissions.Callback) joinPoint.getArgs()[1];
        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {
            }
        });
        new JsPermissionImpl().onGeolocationPermissionsShowPrompt(origin, callback);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.flutter.plugins.webviewflutter.WebChromeClientHostApiImpl$WebChromeClientImpl",
        methodName = {"onShowFileChooser"},
        type = MatchType.SELF
)
class FlutterOnShowFileChooserMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return FlutterWebChromeClientAspect.interceptOnShowFileChooser(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.flutter.plugins.webviewflutter.WebChromeClientHostApiImpl$WebChromeClientImpl",
        methodName = {"onPermissionRequest"},
        type = MatchType.SELF
)
class FlutterOnPermissionRequestMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        FlutterWebChromeClientAspect.interceptOnPermissionRequest(joinPoint);
        return null;
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.flutter.plugins.webviewflutter.WebChromeClientHostApiImpl$WebChromeClientImpl",
        methodName = {"onGeolocationPermissionsShowPrompt"},
        type = MatchType.SELF
)
class FlutterOnGeolocationPermissionsMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        FlutterWebChromeClientAspect.interceptOnGeolocationPermissionsShowPrompt(joinPoint);
        return null;
    }
}
