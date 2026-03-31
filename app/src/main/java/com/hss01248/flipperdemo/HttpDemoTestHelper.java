package com.hss01248.flipperdemo;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * 集中发起 GET / POST（表单、JSON、XML）示例请求，便于在 Flipper Network 里观察。
 * 默认使用 {@code https://httpbin.org}；若网络不可达，可换 {@link #BASE_ECHO} 等同类服务。
 */
public final class HttpDemoTestHelper {

    private static final String TAG = "HttpDemoTestHelper";

    /** httpbin：回显 query、headers、body */
    public static final String BASE_HTTPBIN = "https://httpbin.org";

    /** Postman Echo：部分网络环境下可作为备选 */
    public static final String BASE_ECHO = "https://postman-echo.com";

    private final String baseUrl;
    private final OkHttpClient client;

    public HttpDemoTestHelper() {
        this(BASE_HTTPBIN);
    }

    public HttpDemoTestHelper(@NonNull String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message -> Log.d(TAG, message));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        this.client =
                new OkHttpClient.Builder()
                        .addInterceptor(logging)
                        .retryOnConnectionFailure(true)
                        .build();
    }

    @NonNull
    public OkHttpClient getClient() {
        return client;
    }

    /** 依次发起 GET、表单 POST、JSON POST、XML POST（均为异步 enqueue）。 */
    public void runAllDemos() {
        demoGet(logWrap("GET /get"));
        demoPostForm(logWrap("POST form"));
        demoPostJson(logWrap("POST JSON"));
        demoPostXml(logWrap("POST XML"));
    }

    public void demoGet(@NonNull Callback callback) {
        String url = baseUrl + "/get?flipper=demo&ts=" + System.currentTimeMillis();
        Request request = new Request.Builder().url(url).get().header("X-Demo-Header", "get-sample").build();
        client.newCall(request).enqueue(callback);
    }

    public void demoPostForm(@NonNull Callback callback) {
        RequestBody body =
                new FormBody.Builder()
                        .add("name", "flipper-demo")
                        .add("type", "x-www-form-urlencoded")
                        .build();
        Request request =
                new Request.Builder()
                        .url(baseUrl + "/post")
                        .post(body)
                        .header("X-Demo-Header", "form-post")
                        .build();
        client.newCall(request).enqueue(callback);
    }

    public void demoPostJson(@NonNull Callback callback) {
        String json =
                "{\"plugin\":\"flipper\",\"demo\":true,\"count\":3,\"nested\":{\"a\":1,\"b\":\"two\"}}";
        MediaType jsonType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonType, json);
        Request request =
                new Request.Builder()
                        .url(baseUrl + "/post")
                        .post(body)
                        .header("X-Demo-Header", "json-post")
                        .build();
        client.newCall(request).enqueue(callback);
    }

    public void demoPostXml(@NonNull Callback callback) {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<demoRequest xmlns=\"urn:flipper:demo\">"
                        + "<item id=\"1\" type=\"sample\">hello</item>"
                        + "<meta><source>flipper-demo</source></meta>"
                        + "</demoRequest>";
        MediaType xmlType = MediaType.parse("application/xml; charset=utf-8");
        RequestBody body = RequestBody.create(xmlType, xml);
        Request request =
                new Request.Builder()
                        .url(baseUrl + "/post")
                        .post(body)
                        .header("X-Demo-Header", "xml-post")
                        .header("Accept", "application/xml, application/json, */*")
                        .build();
        client.newCall(request).enqueue(callback);
    }

    private static Callback logWrap(@NonNull final String label) {
        return new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, label + " failed: " + e.getMessage(), e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    Log.i(
                            TAG,
                            label
                                    + " -> "
                                    + response.code()
                                    + " "
                                    + response.message()
                                    + " contentLength="
                                    + (response.body() != null ? response.body().contentLength() : -1));
                } finally {
                    response.close();
                }
            }
        };
    }
}
