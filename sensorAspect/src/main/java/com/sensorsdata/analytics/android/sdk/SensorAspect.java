package com.sensorsdata.analytics.android.sdk;

import android.net.Uri;
import android.text.TextUtils;

import com.sensorsdata.analytics.android.sdk.SAConfigOptions;
import com.sensorsdata.analytics.android.sdk.SALog;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;
import com.sensorsdata.analytics.android.sdk.exceptions.ConnectErrorException;
import com.sensorsdata.analytics.android.sdk.exceptions.ResponseErrorException;
import com.sensorsdata.analytics.android.sdk.util.JSONUtils;
import com.sensorsdata.analytics.android.sdk.util.NetworkUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.sensorsdata.analytics.android.sdk.util.Base64Coder.CHARSET_UTF8;

/**
 * @Despciption todo
 * @Author hss
 * @Date 24/12/2021 09:30
 * @Version 1.0
 */
@Aspect
public class SensorAspect {

    //sendHttpRequest
    static OkHttpClient client;
    private static final String TAG = "SA.AnalyticsMessages2";

    private void initClient() {
        if(client == null){
            OkHttpClient.Builder builder  = new OkHttpClient.Builder();
            if (SensorsDataAPI.sharedInstance().getSSLSocketFactory() != null ) {
                builder.sslSocketFactory(SensorsDataAPI.sharedInstance().getSSLSocketFactory());
            }
            builder.followRedirects(true).followSslRedirects(true);
            client = builder.build();
        }
    }

    @Around("execution(* com.sensorsdata.analytics.android.sdk.AnalyticsMessages.sendHttpRequest(..))")
    public Object setWebViewClient(ProceedingJoinPoint joinPoint) throws Throwable{

       initClient();
        Object[] args = joinPoint.getArgs();
        String path = (String) args[0]; String data = (String) args[1]; String gzip = (String) args[2];
        String rawMessage = (String) args[3]; boolean isRedirects = (boolean) args[4];

        Request.Builder requestBuilder = new Request.Builder();

        if (SensorsDataAPI.sharedInstance().getDebugMode() == SensorsDataAPI.DebugMode.DEBUG_ONLY) {
            requestBuilder.addHeader("Dry-Run", "true");
        }
        requestBuilder.header("Cookie",SensorsDataAPI.sharedInstance().getCookie(false));
       requestBuilder.url(path);

        Uri.Builder builder = new Uri.Builder();
        //先校验crc
        if (!TextUtils.isEmpty(data)) {
            builder.appendQueryParameter("crc", String.valueOf(data.hashCode()));
        }

        builder.appendQueryParameter("gzip", gzip);
        builder.appendQueryParameter("data_list", data);

        String query = builder.build().getEncodedQuery();
        if (TextUtils.isEmpty(query)) {
            SALog.i(TAG, "TextUtils.isEmpty(query): \n" );
            return null;
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),query);
        requestBuilder.post(body);
        try {
            Response response = client.newCall(requestBuilder.build())
                    .execute();

            if (SALog.isLogEnabled()) {
                //todo 过滤掉一些信息
                String jsonMessage = JSONUtils.formatJson(rawMessage);
                // 状态码 200 - 300 间都认为正确
                if (response.isSuccessful()) {
                    SALog.i(TAG, "valid message: \n" + jsonMessage);
                    //todo 入库供查阅
                } else {
                    SALog.i(TAG, "invalid message: \n" + jsonMessage);
                    SALog.i(TAG, String.format(Locale.CHINA, "ret_code: %d", response.code()));
                    SALog.i(TAG, String.format(Locale.CHINA, "ret_content: %s", response));
                }
            }
            int responseCode = response.code();
            if (responseCode < HttpURLConnection.HTTP_OK || responseCode >= HttpURLConnection.HTTP_MULT_CHOICE) {
                String string = response.message();
                if(response.body() != null){
                    string = response.body().string();
                }
                // 校验错误
                throw new ResponseErrorException(String.format("flush failure with response '%s', the response code is '%d'",
                        string, responseCode), responseCode);
            }
        }catch (Throwable throwable){
            if(SALog.isLogEnabled()){
                throwable.printStackTrace();
            }
            throw new ConnectErrorException(throwable);
        }
        return null;

    }


}
