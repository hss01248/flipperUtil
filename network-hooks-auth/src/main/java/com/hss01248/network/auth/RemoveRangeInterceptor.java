package com.hss01248.network.auth;

import androidx.annotation.Keep;

import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.StringUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * request headers: {Range=[bytes=0-0], User-Agent=[Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36]}
 * response headers: {null=[HTTP/1.0 401 Unauthorized], Content-Type=[text/html; charset=UTF-8], OkHttp-Response-Source=[NETWORK 401], OkHttp-Selected-Protocol=[http/1.0], WWW-Authenticate=[Basic realm="Everything"]}
 */
@Keep
public class RemoveRangeInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        //移除Range=[bytes=0-0]
        String range = request.header("Range");
        if (StringUtils.isEmpty(range)) {
            return chain.proceed(request);
        }
        String path = request.url().encodedPath();
        if (path.contains(".jpg")
                || path.contains(".png")
                || path.contains(".gif")
                || path.contains(".jpeg")
                || path.contains(".JPG")
                || path.contains(".webp")
                || path.contains(".avif")) {
            request = request.newBuilder().removeHeader("Range").build();
            return chain.proceed(request);
        }
        return chain.proceed(request);
    }


}
