package com.hss01248.flipper.urlconnection;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;

/**
 * 从 AndroidManifest {@code <meta-data>} 读取 bypass 域名配置。
 *
 * <pre>{@code
 * <application>
 *     <meta-data
 *         android:name="flipper.urlconnection.bypass_exact_hosts"
 *         android:value="sc.example.com,sc.other.com" />
 *     <meta-data
 *         android:name="flipper.urlconnection.bypass_suffix_hosts"
 *         android:value=".sensorsdata.cn,.custom.com" />
 * </application>
 * }</pre>
 */
public final class BypassHostManifestReader {

  public static final String META_EXACT_HOSTS = "flipper.urlconnection.bypass_exact_hosts";
  public static final String META_SUFFIX_HOSTS = "flipper.urlconnection.bypass_suffix_hosts";

  private static final String TAG = "BypassHostManifest";

  private BypassHostManifestReader() {
  }

  /**
   * 读取 manifest 配置并合并到 {@link BypassHostConfig}。
   * 可重复调用；精确域名会追加，不会清空已有配置。
   */
  public static void apply(Context context) {
    if (context == null) {
      return;
    }
    try {
      ApplicationInfo info = context.getPackageManager()
          .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
      Bundle metaData = info.metaData;
      if (metaData == null) {
        return;
      }
      applyExactHosts(metaData.getString(META_EXACT_HOSTS));
      applySuffixHosts(metaData.getString(META_SUFFIX_HOSTS));
    } catch (PackageManager.NameNotFoundException e) {
      LogUtils.w(TAG, e);
    }
  }

  private static void applyExactHosts(String raw) {
    if (TextUtils.isEmpty(raw)) {
      return;
    }
    for (String host : raw.split(",")) {
      String trimmed = host.trim();
      if (!trimmed.isEmpty()) {
        BypassHostConfig.addExactHost(trimmed);
        LogUtils.d(TAG, "manifest exact host: " + trimmed);
      }
    }
  }

  private static void applySuffixHosts(String raw) {
    if (TextUtils.isEmpty(raw)) {
      return;
    }
    for (String suffix : raw.split(",")) {
      String trimmed = suffix.trim();
      if (!trimmed.isEmpty()) {
        BypassHostConfig.addSuffixHost(trimmed);
        LogUtils.d(TAG, "manifest suffix host: " + trimmed);
      }
    }
  }
}
