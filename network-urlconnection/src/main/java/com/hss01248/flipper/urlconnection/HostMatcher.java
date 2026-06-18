package com.hss01248.flipper.urlconnection;

import java.net.URL;

/**
 * 判断 URL 是否应绕过 OkUrlFactory，走系统原生 HttpURLConnection。
 */
final class HostMatcher {

  private HostMatcher() {
  }

  static boolean shouldBypass(URL url) {
    if (url == null) {
      return false;
    }
    String host = url.getHost();
    if (host == null || host.isEmpty()) {
      return false;
    }
    String lowerHost = host.toLowerCase();
    return BypassHostConfig.isExactHost(lowerHost) || BypassHostConfig.matchesSuffix(lowerHost);
  }
}
