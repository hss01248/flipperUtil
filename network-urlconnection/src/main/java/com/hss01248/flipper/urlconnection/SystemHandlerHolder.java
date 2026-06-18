package com.hss01248.flipper.urlconnection;

import android.os.Build;

import com.blankj.utilcode.util.LogUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存 Android 平台默认的 HTTP/HTTPS URLStreamHandler，供白名单域名 bypass 使用。
 * <p>
 * 必须在 {@link java.net.URL#setURLStreamHandlerFactory} 之前完成初始化。
 */
final class SystemHandlerHolder {

    private static final String TAG = "SystemHandlerHolder";

    private static final ConcurrentHashMap<String, URLStreamHandler> HANDLERS = new ConcurrentHashMap<>();

    private static final String[][] HANDLER_CLASS_CANDIDATES = {
            {"http", "com.android.okhttp.HttpHandler", "libcore.net.http.HttpHandler"},
            {"https", "com.android.okhttp.HttpsHandler", "libcore.net.http.HttpsHandler"}
    };

    private SystemHandlerHolder() {
    }

    static void warmUp() {
        getHandler("http");
        getHandler("https");
    }

    static URLStreamHandler getHandler(String protocol) {
        URLStreamHandler cached = HANDLERS.get(protocol);
        if (cached != null) {
            return cached;
        }
        URLStreamHandler handler = createHandler(protocol);
        if (handler != null) {
            HANDLERS.put(protocol, handler);
        }
        return handler;
    }

    static URLConnection openConnection(URL url) throws IOException {
        URLStreamHandler handler = getHandler(url.getProtocol());
        if (handler == null) {
            throw new IOException("No system URLStreamHandler for protocol: " + url.getProtocol());
        }
        return HandlerConnector.openConnection(handler, url);
    }

    static URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        URLStreamHandler handler = getHandler(url.getProtocol());
        if (handler == null) {
            throw new IOException("No system URLStreamHandler for protocol: " + url.getProtocol());
        }
        return HandlerConnector.openConnection(handler, url, proxy);
    }

    private static URLStreamHandler createHandler(String protocol) {
        for (String[] entry : HANDLER_CLASS_CANDIDATES) {
            if (!entry[0].equals(protocol)) {
                continue;
            }
            for (int i = 1; i < entry.length; i++) {
                String className = entry[i];
                if (Build.VERSION.SDK_INT < 24 && className.startsWith("com.android.okhttp")) {
                    continue;
                }
                URLStreamHandler handler = instantiate(className);
                if (handler != null) {
                    LogUtils.d(TAG, "Using system handler: " + className + " for " + protocol);
                    return handler;
                }
            }
        }
        LogUtils.w(TAG, "Failed to load system handler for protocol: " + protocol);
        return null;
    }

    private static URLStreamHandler instantiate(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            Object instance = clazz.newInstance();
            if (instance instanceof URLStreamHandler) {
                return (URLStreamHandler) instance;
            }
        } catch (Throwable ignored) {
            // try next candidate
        }
        return null;
    }

    /**
     * URLStreamHandler.openConnection 为 protected，需反射调用。
     */
    private static final class HandlerConnector {

        private static final Method OPEN_CONNECTION;
        private static final Method OPEN_CONNECTION_WITH_PROXY;

        static {
            Method open = null;
            Method openWithProxy = null;
            try {
                open = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
                open.setAccessible(true);
                openWithProxy = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class, Proxy.class);
                openWithProxy.setAccessible(true);
            } catch (Throwable e) {
                LogUtils.w(TAG, e);
            }
            OPEN_CONNECTION = open;
            OPEN_CONNECTION_WITH_PROXY = openWithProxy;
        }

        private HandlerConnector() {
        }

        static URLConnection openConnection(URLStreamHandler handler, URL url) throws IOException {
            if (OPEN_CONNECTION == null) {
                throw new IOException("openConnection(URL) not available");
            }
            try {
                return (URLConnection) OPEN_CONNECTION.invoke(handler, url);
            } catch (Throwable e) {
                throw new IOException("Failed to open system connection", e);
            }
        }

        static URLConnection openConnection(URLStreamHandler handler, URL url, Proxy proxy) throws IOException {
            if (OPEN_CONNECTION_WITH_PROXY == null) {
                throw new IOException("openConnection(URL, Proxy) not available");
            }
            try {
                return (URLConnection) OPEN_CONNECTION_WITH_PROXY.invoke(handler, url, proxy);
            } catch (Throwable e) {
                throw new IOException("Failed to open system connection with proxy", e);
            }
        }
    }
}
