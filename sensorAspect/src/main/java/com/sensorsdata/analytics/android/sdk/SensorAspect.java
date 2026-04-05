package com.sensorsdata.analytics.android.sdk;

import android.net.Uri;
import android.text.TextUtils;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;
import com.sensorsdata.analytics.android.sdk.exceptions.ConnectErrorException;
import com.sensorsdata.analytics.android.sdk.exceptions.ResponseErrorException;
import com.sensorsdata.analytics.android.sdk.util.JSONUtils;

import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.GzipSource;
import okio.Okio;

/**
 * 神策上报 HTTP 改为 OkHttp（AndroidAOP 织入 AnalyticsMessages#sendHttpRequest）。
 */
public class SensorAspect {

    static OkHttpClient client;
    private static final String TAG = "SA.AnalyticsMessages2";

    private static void initClient() {
        if (client == null) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (SensorsDataAPI.sharedInstance().getSSLSocketFactory() != null) {
                builder.sslSocketFactory(SensorsDataAPI.sharedInstance().getSSLSocketFactory());
            }
            builder.followRedirects(true).followSslRedirects(true);
            client = builder.build();
        }
    }

    static Object interceptSendHttpRequest(ProceedJoinPoint joinPoint) throws Throwable {
        initClient();
        Object[] args = joinPoint.getArgs();
        String path = (String) args[0];
        String data = (String) args[1];
        String gzip = (String) args[2];
        String rawMessage = (String) args[3];
        boolean isRedirects = (boolean) args[4];

        Request.Builder requestBuilder = new Request.Builder();

        if (SensorsDataAPI.sharedInstance().getDebugMode() == SensorsDataAPI.DebugMode.DEBUG_ONLY) {
            requestBuilder.addHeader("Dry-Run", "true");
        }
        String cookie = SensorsDataAPI.sharedInstance().getCookie(false);
        if (!TextUtils.isEmpty(cookie)) {
            requestBuilder.header("Cookie", cookie);
        }

        requestBuilder.url(path);

        Uri.Builder builder = new Uri.Builder();
        if (!TextUtils.isEmpty(data)) {
            builder.appendQueryParameter("crc", String.valueOf(data.hashCode()));
        }

        builder.appendQueryParameter("gzip", gzip);
        builder.appendQueryParameter("data_list", data);
        try {
            GzipSource gzipSource = new GzipSource(Okio.source(new ByteArrayInputStream(data.getBytes())));
            String str = Okio.buffer(gzipSource).readString(Charset.forName("utf-8"));
            builder.appendQueryParameter("data_list_original", str);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        String query = builder.build().getEncodedQuery();
        if (TextUtils.isEmpty(query)) {
            SALog.i(TAG, "TextUtils.isEmpty(query): \n");
            return null;
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), query);
        requestBuilder.post(body);
        try {
            Response response = client.newCall(requestBuilder.build())
                    .execute();

            if (SALog.isLogEnabled()) {
                String jsonMessage = JSONUtils.formatJson(rawMessage);
                if (response.isSuccessful()) {
                    SALog.i(TAG, "valid message: \n" + jsonMessage);
                } else {
                    SALog.i(TAG, "invalid message: \n" + jsonMessage);
                    SALog.i(TAG, String.format(Locale.CHINA, "ret_code: %d", response.code()));
                    SALog.i(TAG, String.format(Locale.CHINA, "ret_content: %s", response));
                }
            }
            int responseCode = response.code();
            if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                String string = response.message();
                if (response.body() != null) {
                    string = response.body().string();
                }
                throw new ResponseErrorException(String.format("flush failure with response '%s', the response code is '%d'",
                        string, responseCode), responseCode);
            }
        } catch (Throwable throwable) {
            if (SALog.isLogEnabled()) {
                throwable.printStackTrace();
            }
            throw new ConnectErrorException(throwable);
        }
        return null;
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "com.sensorsdata.analytics.android.sdk.AnalyticsMessages",
        methodName = {"sendHttpRequest"},
        type = MatchType.SELF
)
class SensorAnalyticsHttpMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return SensorAspect.interceptSendHttpRequest(joinPoint);
    }
}
