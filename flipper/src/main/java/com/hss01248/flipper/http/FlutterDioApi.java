package com.hss01248.flipper.http;

import androidx.annotation.Keep;

import com.blankj.utilcode.util.LogUtils;
import com.facebook.flipper.plugins.network.NetworkReporter;
import com.hss01248.flipper.FlipperUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Despciption todo
 * @Author hss
 * @Date 28/09/2022 11:07
 * @Version 1.0
 */
@Keep
public class FlutterDioApi {


    public static void reportRequest(String requestId, Long timeStamp, String uri, String method, Map<String, String> headers, String body) {

        if (FlipperUtil.getNetworkFlipperPlugin() == null) {
            LogUtils.w("FlipperUtil.getNetworkFlipperPlugin() == null");
            return;
        }
        FlipperUtil.getNetworkFlipperPlugin().reportRequest(buildRequestInfo(requestId, timeStamp, uri, method, headers, body));
    }


    public static void reportResponse(String requestId, Long timeStamp, Long statusCode, String statusReason, Map<String, String> headers, String body) {
        if (FlipperUtil.getNetworkFlipperPlugin() == null) {
            LogUtils.w("FlipperUtil.getNetworkFlipperPlugin() == null");
            return;
        }
        FlipperUtil.getNetworkFlipperPlugin().reportResponse(buildResponseInfo(requestId, timeStamp, statusCode, statusReason, headers, body));

    }

    private static NetworkReporter.RequestInfo buildRequestInfo(String requestId, Long timeStamp, String uri, String method, Map<String, String> headers, String body) {
        NetworkReporter.RequestInfo info = new NetworkReporter.RequestInfo();
        info.requestId = requestId;
        info.body = body.getBytes(StandardCharsets.UTF_8);
        info.headers = buildHeaders(headers);
        info.timeStamp = timeStamp;
        info.method  = method;
        info.uri = uri;

        return info;
    }

    private static List<NetworkReporter.Header> buildHeaders(Map<String, String> headers) {
        if(headers ==null || headers.isEmpty()){
            return new ArrayList<>();
        }
        List<NetworkReporter.Header> list = new ArrayList<>();
        for (String s : headers.keySet()) {
            NetworkReporter.Header header = new NetworkReporter.Header(s,headers.get(s));
            list.add(header);
        }
        return list;
    }

    private static NetworkReporter.ResponseInfo buildResponseInfo(String requestId, Long timeStamp, Long statusCode, String statusReason, Map<String, String> headers, String body) {
        NetworkReporter.ResponseInfo info = new NetworkReporter.ResponseInfo();
        info.requestId = requestId;
        info.body = body.getBytes(StandardCharsets.UTF_8);
        info.headers = buildHeaders(headers);
        info.timeStamp = timeStamp;
        info.statusCode  = (int)statusCode.intValue();
        info.statusReason = statusReason;
        return  info;
    }


}





