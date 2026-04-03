package com.facebook.flipper.plugins.network;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.EventListener;


public class FlipperPerfEventListener extends EventListener {

    private static final int MAX_DNS_ADDRS_CHARS = 2048;

    public static final Map<Call, CallTimings> CALL_TIMINGS_MAP = java.util.Collections.synchronizedMap(new java.util.LinkedHashMap<Call, CallTimings>(1000, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Call, CallTimings> eldest) {
            return size() > 1000;
        }
    });

    /**
     * Ensures this {@link Call} has a timings slot. Used when the client was not built with
     * {@link FlipperPerfEventListenerFactory} (e.g. {@code new OkHttpClient()} bypasses Aspect hooks).
     * A real {@link FlipperPerfEventListener} still replaces this entry in {@link #FlipperPerfEventListener}.
     */
    public static void ensureCallTracked(Call call) {
        if (call == null) {
            return;
        }
        synchronized (CALL_TIMINGS_MAP) {
            if (!CALL_TIMINGS_MAP.containsKey(call)) {
                CALL_TIMINGS_MAP.put(call, new CallTimings());
            }
        }
    }

    public static class CallTimings {
        public long callStartMs;
        public long dnsStartMs;
        public long dnsEndMs;
        public long connectStartMs;
        public long secureConnectStartMs;
        public long secureConnectEndMs;
        public long connectEndMs;
        public long requestHeadersStartMs;
        public long requestHeadersEndMs;
        public long requestBodyStartMs;
        public long requestBodyEndMs;
        public long responseHeadersStartMs;
        public long responseHeadersEndMs;
        public long responseBodyStartMs;
        public long responseBodyEndMs;
        public long callEndMs;
        public long callFailedMs;
        /** Last DNS hostname for this call (OkHttp may invoke DNS more than once). */
        public String dnsHost;
        /** Comma-separated resolved addresses; truncated if very long. */
        public String dnsAddresses;
        public String tlsVersion;
        public String tlsCipherSuite;
        /**
         * Address OkHttp actually opened a socket to (chosen route). May differ from order in
         * {@link #dnsAddresses}. Empty when connection was pooled (no connect* events for this call).
         */
        public String connectPeerIp;
        public int connectPeerPort;
    }

    /**
     * Records the peer OkHttp is connecting to for this attempt (the selected IP/host for this route).
     */
    private static void recordConnectPeer(CallTimings timings, InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return;
        }
        InetAddress addr = inetSocketAddress.getAddress();
        if (addr != null) {
            timings.connectPeerIp = addr.getHostAddress();
        } else {
            timings.connectPeerIp = inetSocketAddress.getHostString();
        }
        timings.connectPeerPort = inetSocketAddress.getPort();
    }

    private static String formatInetAddresses(List<InetAddress> inetAddressList, int maxChars) {
        if (inetAddressList == null || inetAddressList.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < inetAddressList.size(); i++) {
            InetAddress a = inetAddressList.get(i);
            if (a == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(a.getHostAddress());
        }
        if (sb.length() == 0) {
            return null;
        }
        if (sb.length() > maxChars) {
            return sb.substring(0, maxChars - 3) + "...";
        }
        return sb.toString();
    }

    private final EventListener original;
    private final CallTimings timings;

    public FlipperPerfEventListener(EventListener original, Call call) {
        this.original = original;
        this.timings = new CallTimings();
        CALL_TIMINGS_MAP.put(call, timings);
    }

    @Override
    public void callStart(Call call) {
        timings.callStartMs = System.currentTimeMillis();
        if (original != null) {
            original.callStart(call);
        }
    }

    @Override
    public void dnsStart(Call call, String domainName) {
        timings.dnsStartMs = System.currentTimeMillis();
        if (domainName != null && !domainName.isEmpty()) {
            timings.dnsHost = domainName;
        }
        if (original != null) {
            original.dnsStart(call, domainName);
        }
    }

    @Override
    public void dnsEnd(Call call, String domainName, java.util.List<java.net.InetAddress> inetAddressList) {
        timings.dnsEndMs = System.currentTimeMillis();
        if (domainName != null && !domainName.isEmpty()) {
            timings.dnsHost = domainName;
        }
        timings.dnsAddresses = formatInetAddresses(inetAddressList, MAX_DNS_ADDRS_CHARS);
        if (original != null) {
            original.dnsEnd(call, domainName, inetAddressList);
        }
    }

    @Override
    public void connectStart(Call call, java.net.InetSocketAddress inetSocketAddress, java.net.Proxy proxy) {
        timings.connectStartMs = System.currentTimeMillis();
        recordConnectPeer(timings, inetSocketAddress);
        if (original != null) {
            original.connectStart(call, inetSocketAddress, proxy);
        }
    }

    @Override
    public void secureConnectStart(Call call) {
        timings.secureConnectStartMs = System.currentTimeMillis();
        if (original != null) {
            original.secureConnectStart(call);
        }
    }

    @Override
    public void secureConnectEnd(Call call, okhttp3.Handshake handshake) {
        timings.secureConnectEndMs = System.currentTimeMillis();
        if (handshake != null) {
            if (handshake.tlsVersion() != null) {
                timings.tlsVersion = handshake.tlsVersion().javaName();
            }
            if (handshake.cipherSuite() != null) {
                timings.tlsCipherSuite = handshake.cipherSuite().javaName();
            }
        }
        if (original != null) {
            original.secureConnectEnd(call, handshake);
        }
    }

    @Override
    public void connectEnd(Call call, java.net.InetSocketAddress inetSocketAddress, java.net.Proxy proxy, okhttp3.Protocol protocol) {
        timings.connectEndMs = System.currentTimeMillis();
        recordConnectPeer(timings, inetSocketAddress);
        if (original != null) {
            original.connectEnd(call, inetSocketAddress, proxy, protocol);
        }
    }

    @Override
    public void connectFailed(Call call, java.net.InetSocketAddress inetSocketAddress, java.net.Proxy proxy, okhttp3.Protocol protocol, java.io.IOException ioe) {
        if (original != null) {
            original.connectFailed(call, inetSocketAddress, proxy, protocol, ioe);
        }
    }

    @Override
    public void connectionAcquired(Call call, okhttp3.Connection connection) {
        if (original != null) {
            original.connectionAcquired(call, connection);
        }
    }

    @Override
    public void connectionReleased(Call call, okhttp3.Connection connection) {
        if (original != null) {
            original.connectionReleased(call, connection);
        }
    }

    @Override
    public void requestHeadersStart(Call call) {
        timings.requestHeadersStartMs = System.currentTimeMillis();
        if (original != null) {
            original.requestHeadersStart(call);
        }
    }

    @Override
    public void requestHeadersEnd(Call call, okhttp3.Request request) {
        timings.requestHeadersEndMs = System.currentTimeMillis();
        if (original != null) {
            original.requestHeadersEnd(call, request);
        }
    }

    @Override
    public void requestBodyStart(Call call) {
        timings.requestBodyStartMs = System.currentTimeMillis();
        if (original != null) {
            original.requestBodyStart(call);
        }
    }

    @Override
    public void requestBodyEnd(Call call, long byteCount) {
        timings.requestBodyEndMs = System.currentTimeMillis();
        if (original != null) {
            original.requestBodyEnd(call, byteCount);
        }
    }

    @Override
    public void responseHeadersStart(Call call) {
        timings.responseHeadersStartMs = System.currentTimeMillis();
        if (original != null) {
            original.responseHeadersStart(call);
        }
    }

    @Override
    public void responseHeadersEnd(Call call, okhttp3.Response response) {
        timings.responseHeadersEndMs = System.currentTimeMillis();
        if (original != null) {
            original.responseHeadersEnd(call, response);
        }
    }

    @Override
    public void responseBodyStart(Call call) {
        timings.responseBodyStartMs = System.currentTimeMillis();
        if (original != null) {
            original.responseBodyStart(call);
        }
    }

    @Override
    public void responseBodyEnd(Call call, long byteCount) {
        timings.responseBodyEndMs = System.currentTimeMillis();
        if (original != null) {
            original.responseBodyEnd(call, byteCount);
        }
        // OkHttp fires this while the body is still being read in FlipperOkhttpInterceptor; removing
        // here would clear CALL_TIMINGS_MAP before appendPerfHeaders runs.
    }

    @Override
    public void callEnd(Call call) {
        timings.callEndMs = System.currentTimeMillis();
        if (original != null) {
            original.callEnd(call);
        }
    }

    @Override
    public void callFailed(Call call, java.io.IOException ioe) {
        timings.callFailedMs = System.currentTimeMillis();
        if (original != null) {
            original.callFailed(call, ioe);
        }
    }
}
