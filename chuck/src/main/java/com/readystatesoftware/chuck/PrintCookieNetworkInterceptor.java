package com.readystatesoftware.chuck;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class PrintCookieNetworkInterceptor implements Interceptor {



    public static final String KEY_REQUEST_COOKIE = "log-request-cookie";
    public static final String KEY_REQUEST_CONTENT_LENGTH = "log-request-Content-Length";
    public static final String KEY_REQUEST_CONTENT_TYPE = "log-request-Content-Type";
    public static final String KEY_REQUEST_ACCEPT_ENCODING = "log-request-Accet-Encoding";

    public static final String KEY_SET_COOKIE = "log-response-cookie";
    public static final String KEY_RESPONSE_CONTENT_LENGTH = "log-response-Content-Length";
    public static final String KEY_RESPONSE_CONTENT_ENCODING = "log-response-Content-Encoding";
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        String requestCookie = request.header("Cookie");
        if(requestCookie == null){
            requestCookie = "";
        }
        String requestContentLength = request.header("Content-Length");
        if(requestContentLength == null){
            requestContentLength = "";
        }
        String requestContentType = request.header("Content-Type");
        if(requestContentType == null){
            requestContentType = "";
        }
        String requestAcceptEncoding = request.header("Accept-Encoding");
        if(requestAcceptEncoding == null){
            requestAcceptEncoding = "";
        }


       Response response =  chain.proceed(request);

        if(!ChuckInterceptor.isEnable()){
            return response;
        }


        List<String> cookieStrings = response.headers().values("Set-Cookie");
        String responseCookie = "";

        if(cookieStrings!= null && !cookieStrings.isEmpty()){
            responseCookie = Arrays.toString(cookieStrings.toArray());
        }
        String length = response.header("Content-Length");
        if(length == null){
            length = "";
        }
        String encoding = response.header("Content-Encoding");
        if(encoding == null){
            encoding = "";
        }


        response = response.newBuilder()
                .header(KEY_REQUEST_ACCEPT_ENCODING,requestAcceptEncoding)
                .header(KEY_REQUEST_CONTENT_LENGTH,requestContentLength)
                .header(KEY_REQUEST_CONTENT_TYPE,requestContentType)
                .header(KEY_REQUEST_COOKIE,requestCookie)
                .header(KEY_SET_COOKIE,responseCookie)
                .header(KEY_RESPONSE_CONTENT_LENGTH,length)
                .header(KEY_RESPONSE_CONTENT_ENCODING,encoding)
                .build();
        return response;
    }
}
