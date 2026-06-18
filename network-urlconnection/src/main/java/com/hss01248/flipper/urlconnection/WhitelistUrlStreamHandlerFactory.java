package com.hss01248.flipper.urlconnection;

import com.blankj.utilcode.util.LogUtils;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import okhttp3.OkUrlFactory;

/**
 * 将 http/https 请求按域名分流：
 * <ul>
 *   <li>白名单域名 → 系统原生 HttpURLConnection</li>
 *   <li>其他域名 → OkUrlFactory 桥接到 OkHttp</li>
 * </ul>
 */
final class WhitelistUrlStreamHandlerFactory implements URLStreamHandlerFactory {

    private static final String TAG = "WhitelistUrlFactory";

    private final OkUrlFactory okUrlFactory;

    WhitelistUrlStreamHandlerFactory(OkUrlFactory okUrlFactory) {
        this.okUrlFactory = okUrlFactory;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (!"http".equals(protocol) && !"https".equals(protocol)) {
            return null;
        }
        final URLStreamHandler okHandler = okUrlFactory.createURLStreamHandler(protocol);
        if (okHandler == null) {
            return null;
        }
        return new BypassURLStreamHandler(protocol, okHandler);
    }

    private static final class BypassURLStreamHandler extends URLStreamHandler {

        private final String protocol;
        private final URLStreamHandler okHandler;

        BypassURLStreamHandler(String protocol, URLStreamHandler okHandler) {
            this.protocol = protocol;
            this.okHandler = okHandler;
        }

        @Override
        protected URLConnection openConnection(URL url) throws IOException {
            if (HostMatcher.shouldBypass(url)) {
                LogUtils.d(TAG, "bypass system handler: " + url);
                return SystemHandlerHolder.openConnection(url);
            }
            return openViaHandler(okHandler, url);
        }

        @Override
        protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
            if (HostMatcher.shouldBypass(url)) {
                LogUtils.d(TAG, "bypass system handler (proxy): " + url);
                return SystemHandlerHolder.openConnection(url, proxy);
            }
            return openViaHandler(okHandler, url, proxy);
        }

        @Override
        protected int getDefaultPort() {
            return "https".equals(protocol) ? 443 : 80;
        }

        private static URLConnection openViaHandler(URLStreamHandler handler, URL url) throws IOException {
            try {
                java.lang.reflect.Method method = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
                method.setAccessible(true);
                return (URLConnection) method.invoke(handler, url);
            } catch (Throwable e) {
                throw new IOException("Failed to open OkHttp bridged connection", e);
            }
        }

        private static URLConnection openViaHandler(URLStreamHandler handler, URL url, Proxy proxy) throws IOException {
            try {
                java.lang.reflect.Method method = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class, Proxy.class);
                method.setAccessible(true);
                return (URLConnection) method.invoke(handler, url, proxy);
            } catch (Throwable e) {
                throw new IOException("Failed to open OkHttp bridged connection with proxy", e);
            }
        }
    }
}
