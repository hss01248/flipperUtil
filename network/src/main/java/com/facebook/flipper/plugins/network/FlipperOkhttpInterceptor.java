/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.flipper.plugins.network;

import android.text.TextUtils;
import android.util.Pair;

import com.facebook.flipper.core.FlipperArray;
import com.facebook.flipper.core.FlipperConnection;
import com.facebook.flipper.core.FlipperObject;
import com.facebook.flipper.core.FlipperReceiver;
import com.facebook.flipper.core.FlipperResponder;
import com.facebook.flipper.plugins.common.BufferingFlipperPlugin;
import com.facebook.flipper.plugins.network.NetworkReporter.RequestInfo;
import com.facebook.flipper.plugins.network.NetworkReporter.ResponseInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hss01248.network.body.meta.interceptor.MyAppHelperInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

public class FlipperOkhttpInterceptor
    implements Interceptor, BufferingFlipperPlugin.MockResponseConnectionListener {

  // By default, limit body size (request or response) reporting to 1MB to avoid
  // OOM
  private static final long DEFAULT_MAX_BODY_BYTES = 1024 * 1024;

  private final long mMaxBodyBytes;

  private final NetworkFlipperPlugin mPlugin;

  private static class PartialRequestInfo extends Pair<String, String> {
    PartialRequestInfo(String url, String method) {
      super(url, method);
    }
  }

  // pair of request url and method
  private Map<PartialRequestInfo, ResponseInfo> mMockResponseMap = new HashMap<>(0);
  private boolean mIsMockResponseSupported;

  public FlipperOkhttpInterceptor(NetworkFlipperPlugin plugin) {
    this(plugin, DEFAULT_MAX_BODY_BYTES, false);
  }

  /**
   * If you want to change the number of bytes displayed for the body, use this
   * constructor
   */
  public FlipperOkhttpInterceptor(NetworkFlipperPlugin plugin, long maxBodyBytes) {
    this(plugin, maxBodyBytes, false);
  }

  /**
   * To support mock response, addIntercept must be used (instead of
   * addNetworkIntercept) to allow
   * short circuit: https://square.github.io/okhttp/interceptors/ *
   */
  public FlipperOkhttpInterceptor(NetworkFlipperPlugin plugin, boolean isMockResponseSupported) {
    this(plugin, DEFAULT_MAX_BODY_BYTES, isMockResponseSupported);
  }

  public FlipperOkhttpInterceptor(
      NetworkFlipperPlugin plugin, long maxBodyBytes, boolean isMockResponseSupported) {
    mPlugin = plugin;
    mMaxBodyBytes = maxBodyBytes;
    mIsMockResponseSupported = isMockResponseSupported;
    if (isMockResponseSupported) {
      mPlugin.setConnectionListener(this);
    }
  }

  @Override
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request request = chain.request();
    final Pair<Request, Buffer> requestWithClonedBody = cloneBodyAndInvalidateRequest(request, "");
    request = requestWithClonedBody.first;
    final String identifier = UUID.randomUUID().toString();
    mPlugin.reportRequest(convertRequest(request.newBuilder().build(), requestWithClonedBody.second, identifier));

    request = MyAppHelperInterceptor.removeDebugHeaders(request);
    // Check if there is a mock response
    final Response mockResponse = mIsMockResponseSupported ? getMockResponse(request) : null;
    Response response = null;
    try {
      response = mockResponse != null ? mockResponse : chain.proceed(request);
      // 统一使用 Tee 模式: 立即返回, body 在调用方消费时拷贝一份给 Flipper
      return wrapResponseWithTee(response, identifier, mockResponse != null);
    } catch (Throwable throwable) {

      final String str = getExceptionToString(throwable);
      // Log.e("ex","jjjjj->"+new String(bytes));
      // clonedBuffer = Okio.buffer(Okio.sink(new
      // ByteArrayOutputStream(bytes.length))).buffer();
      // clonedBuffer.write(bytes);
      /*
       * Buffer responseBody = Okio.buffer(Okio.sink(new
       * ByteArrayOutputStream(bytes.length))).buffer();
       * responseBody.write(bytes);
       */
      ResponseBody body = ResponseBody.create(MediaType.parse("text/plain"), str);

      // Sun, 27 Jun 2021 02:59:30 GMT
      SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
      String date = sdf.format(new Date());

      Response response1 = new Response.Builder().header("exception", throwable.getClass().getName())
          .header("msg", throwable.getMessage() + "")
          .header("Date", date)
          .receivedResponseAtMillis(System.currentTimeMillis())
          .header("Content-Type", "text/plain")
          .code(499).message("exception happened")
          .protocol(Protocol.HTTP_1_1)
          .body(body)
          .request(request)
          .build();

      final Buffer responseBody1 = cloneBodyForResponse(response1, mMaxBodyBytes);
      final ResponseInfo responseInfo = convertResponse(response1, responseBody1, identifier, mockResponse != null);
      mPlugin.reportResponse(responseInfo);
      // throw new IOException(throwable);
      // throw throwable;
      if (throwable instanceof IOException) {
        throw throwable;
      }
      throw new IOException(throwable);
    }

  }

  static String getExceptionToString(Throwable e) {
    if (e == null) {
      return "";
    }
    StringWriter stringWriter = new StringWriter();
    e.printStackTrace(new PrintWriter(stringWriter));
    return stringWriter.toString();
  }

  private static byte[] bodyBufferToByteArray(final Buffer bodyBuffer, final long maxBodyBytes)
      throws IOException {
    return bodyBuffer.readByteArray(Math.min(bodyBuffer.size(), maxBodyBytes));
  }

  static Gson gson = new GsonBuilder().setPrettyPrinting().create();

  /// This method return original Request and body Buffer, while the original
  /// Request may be
  /// invalidated because body may not be read more than once
  private static Pair<Request, Buffer> cloneBodyAndInvalidateRequest(final Request request, String bodyDesc)
      throws IOException {
    //// todo OOM: 最多读取5M数据,如何实现?
    if (request.body() != null) {
      if (request.body().contentLength() < DEFAULT_MAX_BODY_BYTES) {
        final Request.Builder builder = request.newBuilder();
        final MediaType mediaType = request.body().contentType();
        final Buffer originalBuffer = new Buffer();
        // todo 将内部的文件转换为metadata
        /*
         * MultipartBody body = (MultipartBody) request.body();
         * for (MultipartBody.Part part : body.parts()) {
         * RequestBody body1 = part.body();
         * }
         */
        request.body().writeTo(originalBuffer);
        Buffer clonedBuffer = originalBuffer.clone();
        final RequestBody newOriginalBody = RequestBody.create(mediaType, originalBuffer.readByteString());
        return new Pair<>(builder.method(request.method(), newOriginalBody).build(), clonedBuffer);
      } else {
        /*
         * StringBuilder sb = new StringBuilder("request body larger than 1MB:\n");
         * sb.append(gson.toJson(MyAppHelperInterceptor.getRequestBodyMeta(request).
         * toString()));
         */
        final Buffer originalBuffer = new Buffer();
        byte[] bytes = gson.toJson(MyAppHelperInterceptor.getRequestBodyMeta(request)).getBytes();
        // originalBuffer.read(bytes);
        originalBuffer.write(bytes);
        return new Pair<>(request, originalBuffer);
      }
    }
    return new Pair<>(request, null);
  }

  private static void getRequestBodyDesc(Request request, StringBuilder sb) {
    RequestBody body = request.body();
    if (body instanceof MultipartBody) {
      MultipartBody multipartBody = (MultipartBody) body;
      List<MultipartBody.Part> parts = multipartBody.parts();
      if (parts != null) {
        for (MultipartBody.Part part : parts) {
          sb.append(part.headers().toString())
              .append("\n\n");
          // .append(part.body());
        }
      }
    }
  }

  private RequestInfo convertRequest(
      Request request, final Buffer bodyBuffer, final String identifier) throws IOException {

    // if(!TextUtils.isEmpty(bodyDesc)){
    // byte[] bytes = bodyDesc.getBytes();
    // clonedBuffer = Okio.buffer(Okio.sink(new
    // ByteArrayOutputStream(bytes.length))).buffer();
    // clonedBuffer.write(bytes);
    // }

    // Map<String,String> map = BodyUtil.getBodyDesc(request);

    Map map = MyAppHelperInterceptor.getRequestBodyMeta(request);
    if (request.body() != null && request.body().contentLength() > mMaxBodyBytes) {
      map = new HashMap();
    }
    final List<NetworkReporter.Header> headers = convertHeader(request.headers(), map);
    final RequestInfo info = new RequestInfo();
    info.requestId = identifier;
    info.timeStamp = System.currentTimeMillis();
    info.headers = headers;
    info.method = request.method();
    info.uri = request.url().toString();
    if (requestBodyParser != null) {
      if (requestBodyParser.parseRequestBoddy(request, bodyBuffer, info, map)) {
        return info;
      }
    }
    if (bodyBuffer != null) {
      if (request.body() != null && request.body().contentLength() > mMaxBodyBytes) {
        info.body = bodyBufferToByteArray(bodyBuffer, 1024 * 1024 * 1024);

        NetworkReporter.Header header = new NetworkReporter.Header("Content-Type", "application/json");
        NetworkReporter.Header headerRealType = null;
        Iterator<NetworkReporter.Header> iterator = headers.iterator();
        while (iterator.hasNext()) {
          NetworkReporter.Header next = iterator.next();
          if (next.name.equalsIgnoreCase("Content-Type")) {
            headerRealType = new NetworkReporter.Header("real-Content-Type", next.value);
            iterator.remove();
          }
        }
        if (headerRealType != null) {
          headers.add(headerRealType);
        }
        headers.add(header);
      } else {
        info.body = bodyBufferToByteArray(bodyBuffer, mMaxBodyBytes);
        bodyBuffer.close();
      }

    }

    return info;
  }

  /**
   * 适用于使用了应用拦截器加密后,这里解密并打印到flipper中去的情况 修改request不会影响网络请求
   * 
   * @param requestBodyParser
   */
  public static void setRequestBodyParser(RequestBodyParser requestBodyParser) {
    FlipperOkhttpInterceptor.requestBodyParser = requestBodyParser;
  }

  static RequestBodyParser requestBodyParser;

  private static Buffer cloneBodyForResponse(final Response response, long maxBodyBytes)
      throws IOException {
    if (response.body() != null
        && response.body().source() != null
        && response.body().source().buffer() != null) {
      final BufferedSource source = response.body().source();
      source.request(maxBodyBytes);
      return source.buffer().clone();
    }
    return null;
  }

  /**
   * 检测是否为流式响应(SSE、大文件下载等)
   * 这些响应不能阻塞读取,否则会导致 intercept 方法长时间不返回
   */
  private static boolean isStreamingResponse(Response response) {
    if (response == null || response.body() == null) {
      return false;
    }

    String contentType = response.header("Content-Type");

    // 检测 SSE (Server-Sent Events)
    if (contentType != null && contentType.contains("text/event-stream")) {
      return true;
    }

    // 检测媒体文件和压缩文件 (image/video/audio/压缩文件)
    if (contentType != null) {
      String lowerContentType = contentType.toLowerCase();
      if (lowerContentType.startsWith("image/")
          || lowerContentType.startsWith("video/")
          || lowerContentType.startsWith("audio/")
          || lowerContentType.contains("application/zip")
          || lowerContentType.contains("application/x-rar")
          || lowerContentType.contains("application/x-7z")
          || lowerContentType.contains("application/x-gzip")
          || lowerContentType.contains("application/x-tar")
          || lowerContentType.contains("application/octet-stream")) {
        return true;
      }
    }

    // 检测 chunked 编码 (通常用于流式传输)
    String transferEncoding = response.header("Transfer-Encoding");
    if ("chunked".equalsIgnoreCase(transferEncoding)) {
      return true;
    }

    // 检测大文件 (>10MB),避免阻塞等待大文件下载完成
    long contentLength = response.body().contentLength();
    if (contentLength > 10 * 1024 * 1024) {
      return true;
    }

    return false;
  }

  /**
   * 使用 Tee 模式包装响应: 在调用方消费 body 时,同步拷贝一份给 Flipper
   * 拦截器立即返回,不阻塞等待 body 读取完成
   */
  private Response wrapResponseWithTee(final Response response, final String identifier, final boolean isMock) {
    final ResponseBody originalBody = response.body();
    if (originalBody == null) {
      // body 为空,直接上报响应头信息
      try {
        mPlugin.reportResponse(convertResponse(response, null, identifier, isMock));
      } catch (IOException e) {
        e.printStackTrace();
      }
      return response;
    }

    // 创建日志 buffer,用于记录流式数据
    final Buffer loggingBuffer = new Buffer();
    final long[] totalBytesRead = { 0 };
    final boolean[] hasReported = { false };

    // 创建包装的 ResponseBody
    ResponseBody wrappedBody = new ResponseBody() {
      @Override
      public MediaType contentType() {
        return originalBody.contentType();
      }

      @Override
      public long contentLength() {
        return originalBody.contentLength();
      }

      @Override
      public BufferedSource source() {
        return Okio.buffer(new ForwardingSource(originalBody.source()) {
          @Override
          public long read(Buffer sink, long byteCount) throws IOException {
            long bytesRead = super.read(sink, byteCount);

            if (bytesRead > 0) {
              totalBytesRead[0] += bytesRead;

              // 同时写入日志 buffer (只记录前 1MB)
              if (loggingBuffer.size() < mMaxBodyBytes) {
                long bytesToLog = Math.min(bytesRead, mMaxBodyBytes - loggingBuffer.size());
                // 使用 copyTo 做范围复制,不克隆整个 buffer
                sink.copyTo(loggingBuffer, sink.size() - bytesRead, bytesToLog);
              }
            }

            // 流结束时上报完整信息
            if (bytesRead == -1 && !hasReported[0]) {
              hasReported[0] = true;
              try {
                ResponseInfo responseInfo = convertResponse(response, loggingBuffer, identifier, isMock);
                // 添加流式传输的元数据
                responseInfo.headers
                    .add(new NetworkReporter.Header("X-Stream-Total-Bytes", String.valueOf(totalBytesRead[0])));
                responseInfo.headers
                    .add(new NetworkReporter.Header("X-Stream-Logged-Bytes", String.valueOf(loggingBuffer.size())));
                mPlugin.reportResponse(responseInfo);
              } catch (Exception e) {
                e.printStackTrace();
              }
            }

            return bytesRead;
          }
        });
      }
    };

    return response.newBuilder()
        .body(wrappedBody)
        .build();
  }

  private ResponseInfo convertResponse(
      Response response, Buffer bodyBuffer, String identifier, boolean isMock) throws IOException {
    final List<NetworkReporter.Header> headers = convertHeader(response.headers(), null);
    final ResponseInfo info = new ResponseInfo();
    info.requestId = identifier;
    info.timeStamp = response.receivedResponseAtMillis();
    info.statusCode = response.code();
    info.headers = headers;
    info.isMock = isMock;
    if (bodyBuffer != null) {
      info.body = bodyBufferToByteArray(bodyBuffer, mMaxBodyBytes);
      bodyBuffer.close();
    }

    return info;
  }

  private static List<NetworkReporter.Header> convertHeader(Headers headers, Map metaMap) {
    final List<NetworkReporter.Header> list = new ArrayList<>(headers.size());

    final Set<String> keys = headers.names();
    for (final String key : keys) {
      // if(key.equals("Content-Type") ){
      // String type = headers.get(key);
      // if(!type.contains("text") && !type.contains("application/json")){
      // list.add(new NetworkReporter.Header(key, "application/json"));
      // list.add(new NetworkReporter.Header("realsend-Content-Type", type));
      // continue;
      // }
      // }
      list.add(new NetworkReporter.Header(key, headers.get(key)));
    }
    if (metaMap != null) {
      for (Object key : metaMap.keySet()) {
        list.add(new NetworkReporter.Header("meta-" + key, metaMap.get(key) + ""));
      }
    }
    return list;
  }

  private void registerMockResponse(PartialRequestInfo partialRequest, ResponseInfo response) {
    if (!mMockResponseMap.containsKey(partialRequest)) {
      mMockResponseMap.put(partialRequest, response);
    }
  }

  @Nullable
  private Response getMockResponse(Request request) {
    final String url = request.url().toString();
    final String method = request.method();
    final PartialRequestInfo partialRequest = new PartialRequestInfo(url, method);

    if (!mMockResponseMap.containsKey(partialRequest)) {
      return null;
    }
    ResponseInfo mockResponse = mMockResponseMap.get(partialRequest);
    if (mockResponse == null) {
      return null;
    }

    final Response.Builder builder = new Response.Builder();
    builder
        .request(request)
        .protocol(Protocol.HTTP_1_1)
        .code(mockResponse.statusCode)
        .message(mockResponse.statusReason)
        .receivedResponseAtMillis(System.currentTimeMillis())
        .body(ResponseBody.create(MediaType.parse("application/text"), mockResponse.body));

    if (mockResponse.headers != null && !mockResponse.headers.isEmpty()) {
      for (final NetworkReporter.Header header : mockResponse.headers) {
        if (!TextUtils.isEmpty(header.name) && !TextUtils.isEmpty(header.value)) {
          builder.header(header.name, header.value);
        }
      }
    }
    return builder.build();
  }

  @Nullable
  private ResponseInfo convertFlipperObjectRouteToResponseInfo(FlipperObject route) {
    final String data = route.getString("data");
    final String requestUrl = route.getString("requestUrl");
    final String method = route.getString("method");
    FlipperArray headersArray = route.getArray("headers");
    if (TextUtils.isEmpty(requestUrl) || TextUtils.isEmpty(method)) {
      return null;
    }
    final int statusCode = route.getInt("status");

    final ResponseInfo mockResponse = new ResponseInfo();
    mockResponse.body = data.getBytes();
    mockResponse.statusCode = HttpURLConnection.HTTP_OK;
    mockResponse.statusCode = statusCode;
    mockResponse.statusReason = "OK";
    if (headersArray != null) {
      final List<NetworkReporter.Header> headers = new ArrayList<>();
      for (int j = 0; j < headersArray.length(); j++) {
        final FlipperObject header = headersArray.getObject(j);
        headers.add(new NetworkReporter.Header(header.getString("key"), header.getString("value")));
      }
      mockResponse.headers = headers;
    }
    return mockResponse;
  }

  @Override
  public void onConnect(FlipperConnection connection) {
    connection.receive(
        "mockResponses",
        new FlipperReceiver() {
          @Override
          public void onReceive(FlipperObject params, FlipperResponder responder) throws Exception {
            FlipperArray array = params.getArray("routes");
            mMockResponseMap.clear();
            for (int i = 0; i < array.length(); i++) {
              final FlipperObject route = array.getObject(i);
              final String requestUrl = route.getString("requestUrl");
              final String method = route.getString("method");
              ResponseInfo mockResponse = convertFlipperObjectRouteToResponseInfo(route);
              if (mockResponse != null) {
                registerMockResponse(new PartialRequestInfo(requestUrl, method), mockResponse);
              }
            }
            responder.success();
          }
        });
  }

  @Override
  public void onDisconnect() {
    mMockResponseMap.clear();
  }
}
