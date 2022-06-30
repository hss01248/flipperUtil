package com.hss01248.http.ssl.ignore;


import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2016/12/20 0020.
 */

public class SslUtil {



    /**
     * 让客户端通过所有证书的验证.
     * 注意:容易导致中间人攻击,轻易不要使用
     * 兼容16-19的tls1.1
     *
     * @param httpBuilder
     */
    @SuppressWarnings("deprecation")
    public static void setAllCerPass(OkHttpClient.Builder httpBuilder) {
        X509TrustManager xtm = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                X509Certificate[] x509Certificates = new X509Certificate[]{};
                return x509Certificates;
                // return null;
            }
        };

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");

            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


        HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        httpBuilder.sslSocketFactory(new TLSCompactSocketFactory(sslContext))
                //sslContext.getSocketFactory()
                //.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS))
                .hostnameVerifier(DO_NOT_VERIFY);
    }



}
