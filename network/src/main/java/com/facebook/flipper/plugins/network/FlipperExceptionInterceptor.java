/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.facebook.flipper.plugins.network;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 应用拦截器,用于捕获连接超时/DNS 失败等 NetworkInterceptor 无法感知的异常,
 * 并上报到 Flipper 桌面端。
 *
 * <p>使用方式: 通过 addInterceptor 注册到 OkHttpClient,必须放在最外层(index 0):
 * <pre>
 *   builder.interceptors().add(0, new FlipperExceptionInterceptor(plugin));
 * </pre>
 *
 * <p>去重机制: 通过 {@code flipper-exception-req-id} header 传递 requestId 给
 * {@link FlipperOkhttpInterceptor}。当 NetworkInterceptor 已处理异常时,会将该 id
 * 加入 {@link FlipperOkhttpInterceptor#reportedExceptionIds},本拦截器检查该 Set
 * 来避免重复上报。
 */
public class FlipperExceptionInterceptor implements Interceptor {

	static final String HEADER_EXCEPTION_REQ_ID = "flipper-exception-req-id";

	private final NetworkFlipperPlugin mPlugin;

	public FlipperExceptionInterceptor(NetworkFlipperPlugin plugin) {
		this.mPlugin = plugin;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		final String requestId = UUID.randomUUID().toString();
		final long requestStartMs = System.currentTimeMillis();

		// 将 requestId 附加到 header,传递给 NetworkInterceptor
		// flipper- 前缀的 header 会被 removeDebugHeaders() 自动移除,不会发往服务器
		request = request.newBuilder()
				.header(HEADER_EXCEPTION_REQ_ID, requestId)
				.build();

		try {
			return chain.proceed(request);
		} catch (Throwable throwable) {
			// 检查 NetworkInterceptor 是否已处理
			if (!FlipperOkhttpInterceptor.reportedExceptionIds.remove(requestId)) {
				// NetworkInterceptor 未处理(如连接超时),由本拦截器上报
				reportException(request, requestId, requestStartMs, throwable);
			}
			if (throwable instanceof IOException) {
				throw (IOException) throwable;
			}
			throw new IOException(throwable);
		}
	}

	private void reportException(Request request, String requestId, long requestStartMs, Throwable throwable) {
		try {
			// 上报 RequestInfo
			final NetworkReporter.RequestInfo requestInfo = new NetworkReporter.RequestInfo();
			requestInfo.requestId = requestId;
			requestInfo.timeStamp = requestStartMs;
			requestInfo.method = request.method();
			requestInfo.uri = request.url().toString();
			requestInfo.headers = convertHeaders(request);
			mPlugin.reportRequest(requestInfo);

			// 构造伪 499 ResponseInfo
			final long now = System.currentTimeMillis();
			final String stackTrace = FlipperOkhttpInterceptor.getExceptionToString(throwable);

			final NetworkReporter.ResponseInfo responseInfo = new NetworkReporter.ResponseInfo();
			responseInfo.requestId = requestId;
			responseInfo.timeStamp = now;
			responseInfo.statusCode = 499;
			responseInfo.statusReason = "exception happened";
			responseInfo.body = stackTrace.getBytes();

			List<NetworkReporter.Header> headers = new ArrayList<>();
			headers.add(new NetworkReporter.Header("exception", throwable.getClass().getName()));
			headers.add(new NetworkReporter.Header("msg", String.valueOf(throwable.getMessage())));
			SimpleDateFormat sdf = new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
			headers.add(new NetworkReporter.Header("Date", sdf.format(new Date())));
			headers.add(new NetworkReporter.Header("Content-Type", "text/plain"));
			headers.add(new NetworkReporter.Header("flipper-client-request-start-ms", String.valueOf(requestStartMs)));
			headers.add(new NetworkReporter.Header("flipper-client-response-received-ms", String.valueOf(now)));
			headers.add(new NetworkReporter.Header("flipper-client-elapsed-ms", String.valueOf(now - requestStartMs)));
			headers.add(new NetworkReporter.Header("X-Flipper-Source", "ExceptionInterceptor"));
			responseInfo.headers = headers;

			mPlugin.reportResponse(responseInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static List<NetworkReporter.Header> convertHeaders(Request request) {
		List<NetworkReporter.Header> list = new ArrayList<>();
		for (int i = 0; i < request.headers().size(); i++) {
			String name = request.headers().name(i);
			// 剔除内部 flipper header,不展示在 Flipper 面板
			if (!name.startsWith("flipper-")) {
				list.add(new NetworkReporter.Header(name, request.headers().value(i)));
			}
		}
		return list;
	}
}
