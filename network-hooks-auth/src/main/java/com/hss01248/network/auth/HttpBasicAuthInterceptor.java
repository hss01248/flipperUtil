package com.hss01248.network.auth;

import androidx.annotation.Keep;

import com.blankj.utilcode.util.EncodeUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPStaticUtils;
import com.blankj.utilcode.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * request headers: {Range=[bytes=0-0], User-Agent=[Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36]}
 * response headers: {null=[HTTP/1.0 401 Unauthorized], Content-Type=[text/html; charset=UTF-8], OkHttp-Response-Source=[NETWORK 401], OkHttp-Selected-Protocol=[http/1.0], WWW-Authenticate=[Basic realm="Everything"]}
 */
@Keep
public class HttpBasicAuthInterceptor implements Interceptor {
   public volatile static Map<String, String> authMap = new HashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String host = request.url().host();
        if (authMap.containsKey(host)) {
            request = request.newBuilder().header("Authorization",
                    authMap.get(host)+"").build();
        }
        Response response = chain.proceed(request);
        if (response.code() != 401) {
            return response;
        }
        String auth = response.header("WWW-Authenticate");
        if (StringUtils.isEmpty(auth)) {
            return response;
        }
        if (!auth.contains("Basic realm=")) {
            LogUtils.i("不包含Basic realm=", auth);
            return response;
        }
        String realm = auth.replace("Basic realm=", "");
        realm = realm.replaceAll("\"", "");

        String[] namePw = getHttpAuthUsernamePassword(host, realm);
        if (namePw == null) {
            return response;
        }

        byte[] bytes = (namePw[0] + ":" + namePw[1]).getBytes();
        String basicAuth = "Basic " + EncodeUtils.base64Encode2String(bytes);
        request = request.newBuilder()
                .header("Authorization", basicAuth).build();
        Response response1 = chain.proceed(request);
        if (response1.code() < 401) {
            authMap.put(host, basicAuth);
        }
        return response1;
    }

    public static String[] getHttpAuthUsernamePassword(String host, String realm) {
        String key = "web-basic-name-" + host + "-" + realm;
        String name = SPStaticUtils.getString(key);
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        String pw = SPStaticUtils.getString(key + "-pw", "");
        pw = new String(EncodeUtils.base64Decode(pw));
        return new String[]{name, pw};
    }
}
