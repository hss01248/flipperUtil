package com.hss01248.network.chucker;


import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.chuckerteam.chucker.internal.data.entity.HttpTransaction;
import com.hss01248.network.body.meta.interceptor.MyAppHelperInterceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import java.io.EOFException;
import java.nio.charset.Charset;

import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.GzipSource;
import okio.Okio;


/**
 * by hss
 * data:2020/7/17
 * desc:
 */
@Aspect
public class ChuckerAspect {

    private static final String TAG = "ChuckerAspect";


    @Around("execution(* com.chuckerteam.chucker.api.ChuckerInterceptor.processRequest(..))" )
    public void processRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        Request request = (Request) joinPoint.getArgs()[0];
        HttpTransaction transaction = (HttpTransaction) joinPoint.getArgs()[1];
        doProcessRequest(request,transaction);
    }

    private void doProcessRequest(Request request, HttpTransaction transaction) throws Exception{


        HttpUrl var10 = request.url();
        transaction.populateUrl(var10);
        transaction.setRequestDate(System.currentTimeMillis());
        transaction.setMethod(request.method());
        Headers var10001 = request.headers();
        transaction.setRequestHeaders(var10001);

        RequestBody requestBody = request.body();
        String contentType0 = "";
        if (requestBody != null) {
            MediaType var12 = requestBody.contentType();
            if (var12 != null) {
                contentType0 = var12.toString();
            }
        }
        transaction.setRequestContentType(contentType0);
        transaction.setRequestPayloadSize(requestBody != null ? requestBody.contentLength() : 0L);

        boolean encodingIsSupported = bodyHasSupportedEncoding(request.headers().get("Content-Encoding"));
        transaction.setRequestBodyPlainText(encodingIsSupported);
        String bodyMeta = MyAppHelperInterceptor.getRequestBodyMetaStr(request);
        if(!TextUtils.isEmpty(bodyMeta)){
            LogUtils.w("使用bodymeta",var10.toString());
            transaction.setRequestBody(bodyMeta);
            return;
        }
        Charset UTF8 = Charset.forName("UTF-8");
        if (requestBody != null && encodingIsSupported) {
            BufferedSource source = getNativeSource((BufferedSource)(new Buffer()), isGzipped(request.headers().get("Content-Encoding")));
            Buffer buffer = source.buffer();
            requestBody.writeTo((BufferedSink)buffer);
            Charset var10000 = UTF8;
            Charset charset = var10000;
            MediaType contentType = requestBody.contentType();
            if (contentType != null) {
                var10000 = contentType.charset(UTF8);
                if (var10000 == null) {
                    var10000 = UTF8;
                }

                charset = var10000;
            }

            if (isPlaintext(buffer)) {
                String content = readFromBuffer(buffer, charset, 512*1024L);
                transaction.setRequestBody(content);
            } else {
                transaction.setResponseBodyPlainText(false);
            }
        }
    }

    String readFromBuffer(Buffer buffer , Charset charset , Long maxContentLength ) {
        long  bufferSize = buffer.size();
        long  maxBytes = Math.min(bufferSize, maxContentLength);
        String  body = "";
        try {
            body = buffer.readString(maxBytes, charset);
        } catch (EOFException e) {
            body += StringUtils.getString(R.string.chucker_body_unexpected_eof);
        }

        if (bufferSize > maxContentLength) {
            body += StringUtils.getString(R.string.chucker_body_content_truncated);
        }
        return body;
    }
    private static long PREFIX_SIZE = 64L;
    private static int CODE_POINT_SIZE = 16;
    boolean isPlaintext(Buffer buffer ) {
        try {
            Buffer prefix = new Buffer();
            long byteCount =  (buffer.size() < PREFIX_SIZE) ? buffer.size() : PREFIX_SIZE;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0;i< CODE_POINT_SIZE; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int  codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    static BufferedSource getNativeSource(BufferedSource input , Boolean isGzipped ){
        if (isGzipped) {
            GzipSource source = new GzipSource(input);
            return Okio.buffer(source);
        } else {
            return input;
        }
    }

    private boolean bodyHasSupportedEncoding(String s) {
        if(TextUtils.isEmpty(s)){
            return true;
        }
        if(s.contains("identity") || s.contains("gzip")){
            return true;
        }
        return false;
    }

    private boolean isGzipped(String s) {
        if(TextUtils.isEmpty(s)){
            return false;
        }
        if(s.contains("gzip")){
            return true;
        }
        return false;
    }


}
