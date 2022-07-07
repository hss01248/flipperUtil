package com.readystatesoftware.chuck;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;

public class SameRequestInterceptor implements Interceptor {

    static WeakHashMap<String, ResonseForClone> responseWeakHashMap = new WeakHashMap<>();
    static WeakHashMap<String, WeakReference<Call>> calls = new WeakHashMap<>();

    static Charset UTF_8 = Charset.forName("UTF-8");
    static IConfig config;
    static boolean enableFilter;
    static boolean debug;
    static Handler handler;
    static List<String> urlPathsForIntercept;

    /**
     * @param enableFilter 全局开关
     * @param config       配置信息
     */
    public static void config(boolean debug, boolean enableFilter, IConfig config) {
        SameRequestInterceptor.enableFilter = enableFilter;
        SameRequestInterceptor.config = config;
        SameRequestInterceptor.debug = debug;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //全局开关
        if (!enableFilter) {
            return chain.proceed(request);
        }

        if (config == null) {
            return chain.proceed(request);
        }

        if(urlPathsForIntercept == null){
            urlPathsForIntercept = config.urlPathsForIntercept();
        }
        if(urlPathsForIntercept == null || urlPathsForIntercept.isEmpty()){
            return chain.proceed(request);
        }

        //全局配置的urlpath匹配
        boolean intercept = shouldIntercept(urlPathsForIntercept,request);

        if(!intercept){
            return chain.proceed(request);
        }

        //单个请求特殊过滤
        if (!config.shouldInterceptSameRequest(request)) {
            return chain.proceed(request);
        }

        String key = generateKey(request);
        logw("key is :"+key,request.url().toString());

        //递归检查,等待
        return check(chain, request, key);
    }

    private boolean shouldIntercept(List<String> urlPathsForIntercept, Request request) {
        String url = request.url().toString();

        for (String path : urlPathsForIntercept) {
            if (url.contains(path)){
                return true;
            }
        }
        return false;
    }

    private Response check(Chain chain, Request request, String key) throws IOException {
        try {
            //从缓存的call和response中判断要不要等待
            boolean needwait = needwait(key,request.url().toString());

            if (!needwait) {
                if (responseWeakHashMap.containsKey(key)) {
                    return responseWeakHashMap.get(key).getClonedResonse(request);
                } else {
                    //直接执行请求:
                    //将response缓存起来
                    return realExceute(chain, request, key);
                }
            } else {
                Thread.sleep(config.callWaitTimeInMills());
                return check(chain, request, key);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            if (responseWeakHashMap.containsKey(key)) {
                return responseWeakHashMap.get(key).getClonedResonse(request);
            } else {
                //直接执行请求:
                //将response缓存起来
                return realExceute(chain, request, key);
            }
        }
    }

    @NonNull
    private Response realExceute(Chain chain, Request request, final String key) throws IOException {
        calls.put(key, new WeakReference<>(chain.call()));
        try {
            Response response = chain.proceed(request);
            //如何拿到底层bridgeinterceptor的新request?

            if (canCacheResponse(response)) {
                ResponseBody responseBody = response.body();
                BufferedSource source = responseBody.source();
                source.request(responseBody.contentLength() > 0 ? responseBody.contentLength() : Integer.MAX_VALUE);
                //吓人?
                Buffer buffer = source.buffer();
                Charset charset = UTF_8;
                MediaType contentType = responseBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF_8);
                }
                String bodyString = buffer.clone().readString(charset);

                ResponseBody cloneBody = ResponseBody.create(response.body().contentType(), bodyString);

                Response responseClone = response.newBuilder()
                        .body(cloneBody)
                        .header("cachedResonse", "yes")
                        .build();
                responseWeakHashMap.put(key, new ResonseForClone(bodyString, responseClone));
                calls.remove(key);
                final String url = request.url().toString();
                getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //1min后移除缓存的response:
                        responseWeakHashMap.remove(key);
                        logw(config.responseCacheTimeInMills() + "ms 时间到了,清除缓存的response",url);
                    }
                }, config.responseCacheTimeInMills());

            }else {
                calls.remove(key);
            }

            return response;
        }catch (IOException e){
            calls.remove(key);
            //其他非io类型的异常,calls里用软引用自动移除
            throw e;
        }
    }

    private String str(Request request) {
      return   request.getClass().getName() + "@" + Integer.toHexString(request.hashCode());
    }

    private boolean canCacheResponse(Response response) {
        if(response == null){
            return false;
        }
        if(!response.isSuccessful()){
            return false;
        }
        String url = response.request().url().toString();
        if(response.body() == null){
            logw("response 的body为空,不缓存response",url);
            return false;
        }
        if(response.body().contentType() == null){
            logw("response 的contenttype为空,不缓存response",url);
            return false;
        }
        String type = response.body().contentType().type();
        if("text".equals(type) || "application".equals(type)){
            return true;
        }
        logw("response 的contenttype不是text或application类型,而是:"+type+",不缓存response",url);
        return false;
    }

    private static Handler getMainHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    private boolean needwait(String key,String url) {
        if (responseWeakHashMap.containsKey(key)) {
            logw("有缓存的response,直接去读缓存,并组装新的response",url);
            return false;
        }
        if (calls.containsKey(key)) {
            WeakReference<Call> callWeakReference = calls.get(key);
            if (callWeakReference == null) {
                logw("不需要等待,直接发请求 call WeakReference not exist:",url);
                return false;
            }
            Call call = callWeakReference.get();
            if (call == null || call.isCanceled()) {
                logw("不需要等待,直接发请求 call not exist or is canceld:" + call,url);
                return false;
            }
            logw("请求可能正在等待或正在执行-needwait call is running:" + call,url);
            //请求可能正在等待或正在执行
            return true;
        }
        logw("任何地方都没有,不需要等,直接执行请求",url);
        //任何地方都没有,不需要等,直接执行请求
        return false;
    }

    private static void logw(String str,String url) {
        if (debug) {
            Log.w("SameRequest", str+"  "+ url);
        }
    }

    private static String getCookiesStr(HttpUrl url) {
        if(config.getCookieJarInstance() == null){
            return "";
        }

        List<Cookie> cookies = config.getCookieJarInstance().loadForRequest(url);
        if (!cookies.isEmpty()) {
            return cookieHeader(cookies);
        }
        return "";
    }



    private static String cookieHeader(List<Cookie> cookies) {
        StringBuilder cookieHeader = new StringBuilder();
        for (int i = 0, size = cookies.size(); i < size; i++) {
            if (i > 0) {
                cookieHeader.append("; ");
            }
            Cookie cookie = cookies.get(i);
            cookieHeader.append(cookie.name()).append('=').append(cookie.value());
        }
        return cookieHeader.toString();
    }

    /**
     * @param request
     * @return
     */
    private String generateKey(Request request) {
        return config.generateCacheKey(request);
    }


    class ResonseForClone {
        String body;
        Response response;

        public ResonseForClone(String body, Response response) {
            this.body = body;
            this.response = response;
        }

        /**
         * 注意不能用response.newBuilder().xxxx, 新的response只能拷贝老的字符类信息,不要携带其他任何信息,
         * 否则在上层再次使用时可能会崩溃. 同时,request要使用本次的request
         * @param request
         * @return
         */
        public Response getClonedResonse(Request request) {
            ResponseBody cloneBody = ResponseBody.create(response.body().contentType(), body);
            Response responseClone = new Response.Builder()
                    .protocol(response.protocol())
                    .code(response.code())
                    .message(response.message())
                    .headers(response.headers())
                    .body(cloneBody)
                    .request(request)
                    .build();
            return responseClone;
        }
    }

    public static abstract class IConfig {

        /**
         * 在配置了全局url path基础上,对单个request进行更细致的判断
         * @param request
         * @return
         */
        protected  boolean shouldInterceptSameRequest(Request request){
            return true;
        }

        /**
         * 全局配置url path的白名单,只有在这个名单中的,才予以拦截同样的请求
         * @return
         */
        protected List<String>  urlPathsForIntercept(){
            return new ArrayList<>();
        }

        protected String generateCacheKey(Request request){
            //这一层拿不到cookie,得通过外部cookiejar拿
            String cookie = getCookiesStr(request.url());

            if(request.body() == null){
                return request.url().toString()+"&Cookie="+cookie;
            }
            String url = request.url().toString();
            RequestBody body = request.body();
            String type = "";
            if(body.contentType() != null){
                type = body.contentType().toString();
            }
            long length = -1;
            try {
                 length = body.contentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return url+"&ContentType="+type+"&ContentLength="+length+"&Cookie="+cookie;
        }

        /**
         * 应该使用单例. 返回null代表不将cookie内容用作key的生成
         * @return
         */
        protected CookieJar getCookieJarInstance() {
            return null;
        }

        /**
         * 多长时间内相同的请求要读缓存,默认1min
         * @return
         */
        protected long responseCacheTimeInMills(){
            return 60000;
        }

        /**
         * 发现相同请求在执行时,内部线程等多长时间去查询缓存的response,默认500ms
         * @return
         */
        public long callWaitTimeInMills() {
            return 500;
        }
    }
}
