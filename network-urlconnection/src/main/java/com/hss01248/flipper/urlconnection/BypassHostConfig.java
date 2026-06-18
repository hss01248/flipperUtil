package com.hss01248.flipper.urlconnection;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 配置需绕过 OkUrlFactory、走系统 HttpURLConnection 的域名白名单。
 * <p>
 * 须在 {@link ProxyUrlConnectionUtil#proxyUrlConnection()} 之前完成配置。
 *
 * <pre>{@code
 * BypassHostConfig.addExactHost("sc.example.com");
 * BypassHostConfig.addSuffixHost(".example.net");
 * ProxyUrlConnectionUtil.proxyUrlConnection();
 * }</pre>
 */
public final class BypassHostConfig {

  private static final Set<String> EXACT_HOSTS = new CopyOnWriteArraySet<>();
  private static final CopyOnWriteArrayList<String> SUFFIX_HOSTS =
      new CopyOnWriteArrayList<>(Collections.singletonList(".sensorsdata.cn"));

  private BypassHostConfig() {
  }

  /** 完整域名精确匹配，例如 sc.example.com */
  public static void addExactHost(String host) {
    if (host == null || host.isEmpty()) {
      return;
    }
    EXACT_HOSTS.add(host.toLowerCase());
  }

  public static void addExactHosts(Collection<String> hosts) {
    if (hosts == null) {
      return;
    }
    for (String host : hosts) {
      addExactHost(host);
    }
  }

  public static void removeExactHost(String host) {
    if (host != null) {
      EXACT_HOSTS.remove(host.toLowerCase());
    }
  }

  public static void clearExactHosts() {
    EXACT_HOSTS.clear();
  }

  /**
   * 域名后缀匹配，须以 {@code .} 开头，例如 {@code .sensorsdata.cn}
   */
  public static void addSuffixHost(String suffix) {
    if (suffix == null || suffix.isEmpty()) {
      return;
    }
    String normalized = suffix.startsWith(".") ? suffix : "." + suffix;
    if (!SUFFIX_HOSTS.contains(normalized)) {
      SUFFIX_HOSTS.add(normalized.toLowerCase());
    }
  }

  public static void addSuffixHosts(Collection<String> suffixes) {
    if (suffixes == null) {
      return;
    }
    for (String suffix : suffixes) {
      addSuffixHost(suffix);
    }
  }

  public static void removeSuffixHost(String suffix) {
    if (suffix != null) {
      String normalized = suffix.startsWith(".") ? suffix : "." + suffix;
      SUFFIX_HOSTS.remove(normalized.toLowerCase());
    }
  }

  public static void resetSuffixHostsToDefault() {
    SUFFIX_HOSTS.clear();
    SUFFIX_HOSTS.addAll(Collections.singletonList(".sensorsdata.cn"));
  }

  static boolean isExactHost(String lowerHost) {
    return EXACT_HOSTS.contains(lowerHost);
  }

  static boolean matchesSuffix(String lowerHost) {
    for (String suffix : SUFFIX_HOSTS) {
      if (lowerHost.endsWith(suffix)) {
        return true;
      }
    }
    return false;
  }

  static Set<String> getExactHosts() {
    return Collections.unmodifiableSet(EXACT_HOSTS);
  }

  static Collection<String> getSuffixHosts() {
    return Collections.unmodifiableList(SUFFIX_HOSTS);
  }
}
