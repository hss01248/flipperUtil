/*
 * Copyright (C) 2015 Square, Inc, 2017 Jeff Gilfelt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.readystatesoftware.chuck;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.hss01248.network.body.meta.interceptor.MyAppHelperInterceptor;
import com.readystatesoftware.chuck.internal.data.ChuckContentProvider;
import com.readystatesoftware.chuck.internal.data.HttpHeader;
import com.readystatesoftware.chuck.internal.data.HttpTransaction;
import com.readystatesoftware.chuck.internal.data.LocalCupboard;
import com.readystatesoftware.chuck.internal.support.NotificationHelper;
import com.readystatesoftware.chuck.internal.support.RetentionManager;
import com.readystatesoftware.chuck.internal.ui.MainActivity;

import java.io.EOFException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;

/**
 * An OkHttp Interceptor which persists and displays HTTP activity in your application for later inspection.
 */
public final class ChuckInterceptor implements Interceptor {

    public enum Period {
        /**
         * Retain data for the last hour.
         */
        ONE_HOUR,
        /**
         * Retain data for the last day.
         */
        ONE_DAY,
        /**
         * Retain data for the last week.
         */
        ONE_WEEK,
        /**
         * Retain data forever.
         */
        FOREVER
    }

    private static final String LOG_TAG = "ChuckInterceptor";
    private static final Period DEFAULT_RETENTION = Period.ONE_WEEK;
    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final Context context;
    private NotificationHelper notificationHelper;
    private RetentionManager retentionManager;
    private boolean showNotification;
    private long maxContentLength = 250000L;

    private static List<String> filterUrlPattern;

    private static WeakReference<Activity> topActivity;
    private static int activityCnt;
    private static boolean isBackground;

    public static boolean isEnable() {
        return enable;
    }

    /**
     * 是否要开启
     */
    private static boolean enable;


    static String getTopActivity() {
        if (isBackground || topActivity == null || topActivity.get() == null) {
            return "background";
        } else {
            return topActivity.get().getClass().getSimpleName();
        }
    }

    private static int clickCount;
    private static Application app;


    private static IChuckPwCheck pwCheck;

    public static void switcher() {
        if (isApkDebugable()) {
            enable = !enable;
            saveSp(enable);
            Toast.makeText(app, enable ? "通知栏抓包工具已开启" : "通知栏抓包工具已关闭", Toast.LENGTH_SHORT).show();
            return;
        }

        //开的状态，要关的话，点一下就好了
        if (enable) {
            clickCount++;
            if (clickCount < 3) {
                return;
            }
            clickCount = 0;
            Toast.makeText(app, "通知栏抓包工具已关闭", Toast.LENGTH_SHORT).show();
            enable = !enable;
            saveSp(enable);
            return;
        }
        //关的状态，要开的话，需要点击10下，输入密码
        clickCount++;
        if (clickCount < 8) {
            return;
        }
        inputPw();


    }

    private static void inputPw() {
        final EditText editText = new EditText(topActivity.get());
        AlertDialog dialog = new AlertDialog.Builder(topActivity.get())
                .setTitle("发现彩蛋")
                .setMessage("输入邀请码")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String text = editText.getText().toString().trim();
                        if (TextUtils.isEmpty(text)) {
                            Toast.makeText(topActivity.get(), "输入为空！", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        final ProgressDialog dialog1 = new ProgressDialog(topActivity.get());
                        dialog1.setCanceledOnTouchOutside(false);
                        dialog1.show();
                        checkPw(text, dialog1);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
        dialog.setCanceledOnTouchOutside(false);
    }

    private static void checkPw(String text, final ProgressDialog dialog1) {

        try {
            if (pwCheck != null) {
                pwCheck.check(text, new Runnable() {
                    @Override
                    public void run() {
                        enable = true;
                        saveSp(enable);
                        dismiss(dialog1, "成功打开通知栏抓包工具");
                        clickCount = 0;
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        dismiss(dialog1, "");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            dismiss(dialog1, "exception:" + e.getMessage());
        }


    }

    private static void dismiss(final ProgressDialog dialog1, final String pw_error) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                dialog1.dismiss();
                if (TextUtils.isEmpty(pw_error)) {
                    return;
                }
                Toast.makeText(topActivity.get(), pw_error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void saveSp(boolean enable) {
        SharedPreferences.Editor sp = app.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE).edit();
        sp.putBoolean("ckenable", enable).apply();
    }

    public static void init(final Application application, final boolean openChuckDefault, IChuckPwCheck pwCheck) {
        if (application == null) {
            return;
        }
        app = application;
        ChuckInterceptor.pwCheck = pwCheck;
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                topActivity = new WeakReference<>(activity);
            }

            @Override
            public void onActivityStarted(Activity activity) {
                activityCnt++;
                if (activityCnt == 1) {
                    isBackground = false;
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {
                if (activity instanceof MainActivity) {
                    return;
                }
                topActivity = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                activityCnt--;
                if (activityCnt == 0) {
                    isBackground = true;
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = application.getSharedPreferences(LOG_TAG, Context.MODE_PRIVATE);
                enable = sp.getBoolean("ckenable", isApkDebugable() || openChuckDefault);
            }
        }).start();
    }

    public static List<String> addNotInterceptUrlPattern(String pattern) {
        if (filterUrlPattern == null) {
            filterUrlPattern = new ArrayList<>();
        }
        filterUrlPattern.add(pattern);
        return filterUrlPattern;
    }

    private static boolean isApkDebugable() {
        try {
            ApplicationInfo info = app.getApplicationInfo();
            return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * @param context The current Context.
     */
    public ChuckInterceptor(Context context) {
        this.context = context.getApplicationContext();

    }

    private void init2() {
        if (notificationHelper == null) {
            notificationHelper = new NotificationHelper(this.context);
            showNotification = true;
            retentionManager = new RetentionManager(this.context, DEFAULT_RETENTION);
        }

    }

    /**
     * Control whether a notification is shown while HTTP activity is recorded.
     *
     * @param show true to show a notification, false to suppress it.
     * @return The {@link ChuckInterceptor} instance.
     */
    public ChuckInterceptor showNotification(boolean show) {
        showNotification = show;
        return this;
    }

    /**
     * Set the maximum length for request and response content before it is truncated.
     * Warning: setting this value too high may cause unexpected results.
     *
     * @param max the maximum length (in bytes) for request/response content.
     * @return The {@link ChuckInterceptor} instance.
     */
    public ChuckInterceptor maxContentLength(long max) {
        this.maxContentLength = max;
        return this;
    }

    /**
     * Set the retention period for HTTP transaction data captured by this interceptor.
     * The default is one week.
     *
     * @param period the peroid for which to retain HTTP transaction data.
     * @return The {@link ChuckInterceptor} instance.
     */
    public ChuckInterceptor retainDataFor(Period period) {
        retentionManager = new RetentionManager(context, period);
        return this;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Response response = null;
        Request request = chain.request();
        String url = request.url().toString();
        //如果没有开启，那么就什么都不做
        if (!enable) {
            return chain.proceed(request);
        }
        init2();

        //过滤掉一些请求，防止刷屏或者那些不关注的干扰
        if (filterUrlPattern != null && !filterUrlPattern.isEmpty()) {
            boolean contains = false;
            for (String parttern : filterUrlPattern) {
                if (url.contains(parttern)) {
                    contains = true;
                    break;
                }
            }
            if (contains) {
                return chain.proceed(request);
            }
        }

        HttpTransaction transaction = null;
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        transaction = new HttpTransaction();
        transaction.setRequestDate(new Date());

        transaction.setMethod(request.method());
        transaction.setUrl(request.url().toString());
        //发出请求时的页面
        transaction.setPage(ChuckInterceptor.getTopActivity());

        transaction.setRequestHeaders(request.headers());
        if (hasRequestBody) {
            if (requestBody.contentType() != null) {
                transaction.setRequestContentType(requestBody.contentType().toString());
            }
            if (requestBody.contentLength() != -1) {
                transaction.setRequestContentLength(requestBody.contentLength());
            }
        }
        String str = MyAppHelperInterceptor.getRequestBodyMetaStr(request);
        if (!TextUtils.isEmpty(str)) {
            transaction.setResponseBodyIsPlainText(true);
            transaction.setRequestBody(str);
        } else {
            //这里基本走不到....
            transaction.setRequestBodyIsPlainText(!bodyHasUnsupportedEncoding(request.headers()));
            if (hasRequestBody && transaction.requestBodyIsPlainText()) {
                BufferedSource source = getNativeSource(new Buffer(), bodyGzipped(request.headers()));
                Buffer buffer = source.buffer();
                requestBody.writeTo(buffer);
                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                if (isPlaintext(buffer)) {
                    transaction.setRequestBody(readFromBuffer(buffer, charset));
                } else {
                    transaction.setRequestBodyIsPlainText(false);
                }
            }
        }

        Uri transactionUri = create(transaction);//这里也会有sqllite引起crash.可能是由混淆引起的.

        long startNs = System.nanoTime();

        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            transaction.setError(e.toString());
            try {
                update(transaction, transactionUri);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            //不对exception进行包装,让外面框架自行处理,避免信息丢失.
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();

        transaction.setRequestHeaders(response.request().headers()); // includes headers added later in the chain
        transaction.setResponseDate(new Date());
        transaction.setTookMs(tookMs);
        transaction.setProtocol(response.protocol().toString());
        transaction.setResponseCode(response.code());
        transaction.setResponseMessage(response.message());
        transaction.setResponseHeaders(response.headers());
        transaction.setResponseBodyIsPlainText(!bodyHasUnsupportedEncoding(response.headers()));

        if (responseBody != null) {
            transaction.setResponseContentLength(responseBody.contentLength());
            if (responseBody.contentType() != null) {
                transaction.setResponseContentType(responseBody.contentType().toString());
            }
        }

        if (HttpHeaders.hasBody(response) && transaction.responseBodyIsPlainText()) {
            BufferedSource source = getNativeSource(response);
            source.request(Long.MAX_VALUE);
            Buffer buffer = source.buffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                try {
                    charset = contentType.charset(UTF8);
                } catch (UnsupportedCharsetException e) {
                    update(transaction, transactionUri);
                    return response;
                }
            }
            if (isPlaintext(buffer)) {
                transaction.setResponseBody(readFromBuffer(buffer.clone(), charset));
            } else {
                transaction.setResponseBodyIsPlainText(false);
            }
            transaction.setResponseContentLength(buffer.size());
        }

        update(transaction, transactionUri);
        return response;

    }


    /**
     * 记录webview的访问,在onPageFinish中调用. 时间以onpagestart->onpageFinish计.
     *
     * @param webView
     * @param requestMethod
     * @param headers
     * @param requestBody
     * @param tookMs
     * @param protocol
     * @param responseCode
     * @param responseMsg
     * @param responseHeaders
     */
    public void logWebview(final WebView webView, final String url, final String requestMethod, final List<HttpHeader> headers, final String requestBody, final long tookMs,
                           final String protocol, final int responseCode, final String responseMsg,
                           final List<HttpHeader> responseHeaders) {
        if (!enable) {
            return;
        }
        ChuckJsObj.getSourceHtml(webView, new ChuckJsObj.IWebHtmlCallback<String>() {
            @Override
            public void onSuccess(String s) {
                logHttp(true, url, requestMethod, headers, requestBody, tookMs, protocol, responseCode, responseMsg, s, responseHeaders);
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }


    /**
     * 只支持string类型的
     * 可以由其他网络框架传入,也可以记录webview加载的url和时间. 其中webview的如果要获取url,需要注入js,可以用上面的方法:
     *
     * @param url
     * @param requestMethod
     * @param headers
     * @param requestBody
     * @param tookMs
     * @param protocol
     * @param responseCode
     * @param responseMsg
     * @param responseStr
     * @param responseHeaders
     */
    public void logHttp(boolean inWebView, String url, String requestMethod, List<HttpHeader> headers, String requestBody, long tookMs,
                        String protocol, int responseCode, String responseMsg,
                        String responseStr, List<HttpHeader> responseHeaders) {

        try {
            if (!enable) {
                return;
            }
            init2();
            HttpTransaction transaction = null;

            transaction = new HttpTransaction();
            transaction.setRequestDate(new Date());
            transaction.setInWebView(inWebView);

            transaction.setMethod(requestMethod);
            transaction.setUrl(url);
            //发出请求时的页面
            transaction.setPage(ChuckInterceptor.getTopActivity());
            transaction.setRequestHeaders(headers);
            if (!TextUtils.isEmpty(requestBody)) {
                transaction.setRequestBodyIsPlainText(true);
                transaction.setRequestBody(requestBody);
                transaction.setRequestContentLength((long) requestBody.length());
            }
            transaction.setOtherNetWorkFramework(!inWebView);

            //这里也会有sqllite引起crash.可能是由混淆引起的.
            Uri transactionUri = create(transaction);


            //响应
            transaction.setResponseDate(new Date());
            transaction.setTookMs(tookMs);
            transaction.setProtocol(protocol);
            transaction.setResponseCode(responseCode);
            transaction.setResponseMessage(responseMsg);

            if (!TextUtils.isEmpty(responseStr)) {
                transaction.setResponseContentLength((long) responseStr.length());
                transaction.setResponseContentType("application/json");
                transaction.setResponseBodyIsPlainText(true);
                transaction.setResponseBody(responseStr);
            }

            transaction.setResponseHeaders(responseHeaders);

            update(transaction, transactionUri);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

    }

    private Uri create(HttpTransaction transaction) {
        try {
            ContentValues values = LocalCupboard.getInstance().withEntity(HttpTransaction.class).toContentValues(transaction);
            Uri uri = context.getContentResolver().insert(ChuckContentProvider.TRANSACTION_URI, values);
            transaction.setId(Long.valueOf(uri.getLastPathSegment()));
            if (showNotification) {
                notificationHelper.show(transaction);
            }
            retentionManager.doMaintenance();
            return uri;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private int update(HttpTransaction transaction, Uri uri) {
        if (uri == null) {
            Log.w("chuck", "uri is null!");
            return -1;
        }
        ContentValues values = LocalCupboard.getInstance().withEntity(HttpTransaction.class).toContentValues(transaction);
        int updated = context.getContentResolver().update(uri, values, null, null);
        if (showNotification && updated > 0) {
            notificationHelper.show(transaction);
        }
        return updated;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private boolean bodyHasUnsupportedEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null &&
                !contentEncoding.equalsIgnoreCase("identity") &&
                !contentEncoding.equalsIgnoreCase("gzip");
    }

    private boolean bodyGzipped(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return "gzip".equalsIgnoreCase(contentEncoding);
    }

    private String readFromBuffer(Buffer buffer, Charset charset) {
        long bufferSize = buffer.size();
        long maxBytes = Math.min(bufferSize, maxContentLength);
        String body = "";
        try {
            body = buffer.readString(maxBytes, charset);
        } catch (EOFException e) {
            body += context.getString(R.string.chuck_body_unexpected_eof);
        }
        if (bufferSize > maxContentLength) {
            body += context.getString(R.string.chuck_body_content_truncated);
        }
        return body;
    }

    private BufferedSource getNativeSource(BufferedSource input, boolean isGzipped) {
        if (isGzipped) {
            GzipSource source = new GzipSource(input);
            return Okio.buffer(source);
        } else {
            return input;
        }
    }

    private BufferedSource getNativeSource(Response response) throws IOException {
        if (bodyGzipped(response.headers())) {
            BufferedSource source = response.peekBody(maxContentLength).source();
            if (source.buffer().size() < maxContentLength) {
                return getNativeSource(source, true);
            } else {
                Log.w(LOG_TAG, "gzip encoded response was too long");
            }
        }
        return response.body().source();
    }
}
