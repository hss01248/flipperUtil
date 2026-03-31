package com.flipperdemo.sse;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Minimal SSE demo server (JDK {@link HttpServer} only).
 * <p>
 * Default: {@code http://0.0.0.0:18080/sse}
 */
public final class SseHttpServer {

    public static final int DEFAULT_PORT = 18080;
    public static final String SSE_PATH = "/sse";

    private SseHttpServer() {
    }

    public static void main(String[] args) throws IOException {
        final int port = args.length >= 1 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        HttpServer http = HttpServer.create(new InetSocketAddress(port), 0);
        http.createContext(SSE_PATH, new SseHandler());
        http.createContext("/", exchange -> {
            byte[] msg = ("SSE demo — connect to " + SSE_PATH + " (port " + port + ")\n").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, msg.length);
            exchange.getResponseBody().write(msg);
            exchange.close();
        });
        http.setExecutor(Executors.newCachedThreadPool());
        http.start();
        printStartupHints(port);
    }

    /**
     * Prints bind address, emulator shortcut, and each live interface's IPv4 (for physical device on LAN).
     */
    private static void printStartupHints(int port) {
        String urlSuffix = ":" + port + SSE_PATH;
        System.out.println("SSE listening on all interfaces: http://0.0.0.0" + urlSuffix);
        System.out.println("Android emulator (maps to host loopback): http://10.0.2.2" + urlSuffix);
        List<String> lines = new ArrayList<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface nif = interfaces.nextElement();
                if (!nif.isUp() || nif.isLoopback()) {
                    continue;
                }
                String ifName = nif.getName();
                String display = nif.getDisplayName() != null ? nif.getDisplayName() : ifName;
                for (InetAddress addr : Collections.list(nif.getInetAddresses())) {
                    if (!(addr instanceof Inet4Address)) {
                        continue;
                    }
                    if (addr.isLoopbackAddress() || addr.isLinkLocalAddress()) {
                        continue;
                    }
                    String ip = addr.getHostAddress();
                    lines.add("  " + ifName + " (" + display + ")  http://" + ip + urlSuffix);
                }
            }
        } catch (SocketException e) {
            System.err.println("Could not enumerate network interfaces: " + e.getMessage());
        }
        if (lines.isEmpty()) {
            System.out.println("No routable IPv4 found on non-loopback interfaces (enable Wi‑Fi / Ethernet).");
        } else {
            System.out.println("This machine IPv4 — use one URL on a real phone (same LAN / hotspot):");
            lines.forEach(System.out::println);
        }
    }

    static final class SseHandler implements HttpHandler {
        private final AtomicInteger connectionId = new AtomicInteger(0);

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                exchange.close();
                return;
            }
            int id = connectionId.incrementAndGet();
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", "text/event-stream; charset=utf-8");
            headers.set("Cache-Control", "no-cache, no-transform");
            headers.set("Connection", "keep-alive");
            headers.set("X-Accel-Buffering", "no");
            headers.set("Access-Control-Allow-Origin", "*");
            // Chunked body: length 0 means streaming
            exchange.sendResponseHeaders(200, 0);
            OutputStream out = exchange.getResponseBody();
            try {
                sendEvent(out, "connected", "{\"conn\":" + id + ",\"msg\":\"hello from SseHttpServer\"}");
                for (int i = 1; i <= 60; i++) {
                    Thread.sleep(1000);
                    long t = System.currentTimeMillis();
                    sendEvent(out, "tick", "{\"seq\":" + i + ",\"conn\":" + id + ",\"time\":" + t + "}");
                }
                sendEvent(out, "done", "{\"conn\":" + id + ",\"msg\":\"stream finished\"}");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (IOException ignored) {
                // client disconnected
            } finally {
                try {
                    out.close();
                } catch (IOException ignored) {
                }
                exchange.close();
            }
        }

        private static void sendEvent(OutputStream out, String event, String dataJson) throws IOException {
            StringBuilder sb = new StringBuilder();
            if (event != null && !event.isEmpty()) {
                sb.append("event: ").append(event).append('\n');
            }
            sb.append("data: ").append(dataJson).append("\n\n");
            byte[] bytes = sb.toString().getBytes(StandardCharsets.UTF_8);
            out.write(bytes);
            out.flush();
        }
    }
}
