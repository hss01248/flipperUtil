package com.readystatesoftware.chuck;

import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import java.util.WeakHashMap;



public class ChuckJsObj {

   private static WeakHashMap<WebView,ChuckJsObj> map;
   private static final String TAG = "ChuckJsObj";

    public static ChuckJsObj init(WebView webView){
        ChuckJsObj chuckJsObj =  new ChuckJsObj();
        chuckJsObj.webView = webView;
        webView.addJavascriptInterface(chuckJsObj,TAG);
        if(map == null){
            map = new WeakHashMap<>();
        }
        map.put(webView, chuckJsObj);
        return chuckJsObj;
    }

    WebView webView;

    IWebHtmlCallback<String> callback;

    public IWebHtmlCallback<String> getCallback() {
        return callback;
    }

    public void setCallback(IWebHtmlCallback<String> callback) {
        this.callback = callback;
    }

    @JavascriptInterface
    public void showSource(String html){
        html = "<html xmlns=\"http://www.w3.org/1999/xhtml\">"+html+"</html>";
        if(callback != null){
            callback.onSuccess(html);
            setCallback(null);
        }
    }


    /**
     * 用这个方法去触发取源码
     * @param webView
     */
    public static void getSourceHtml(final WebView webView, IWebHtmlCallback<String> callback){
        if(map == null){
            callback.onSuccess("");
            Log.w("ChuckJsObj","map == null");
            return;
        }
        ChuckJsObj jsObj = map.get(webView);
        if(jsObj == null){
            callback.onSuccess("");
            Log.w("ChuckJsObj","map.get(webView) == null");
            return;
        }
        jsObj.setCallback(callback);
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:window."+TAG+".showSource("
                        + "document.getElementsByTagName('html')[0].innerHTML);");
            }
        });


        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(scrip,callbackxxx);
        }*/
    }

    public interface IWebHtmlCallback<T> {
        void onSuccess(T t);

        void onError(Throwable e);
    }
}
