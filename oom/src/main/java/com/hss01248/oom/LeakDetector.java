package com.hss01248.oom;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.kwai.koom.base.CommonConfig;
import com.kwai.koom.base.MonitorLog;
import com.kwai.koom.base.MonitorManager;
import com.kwai.koom.base.loop.LoopMonitor;
import com.kwai.koom.javaoom.monitor.OOMHprofUploader;
import com.kwai.koom.javaoom.monitor.OOMMonitor;
import com.kwai.koom.javaoom.monitor.OOMMonitorConfig;
import com.kwai.koom.javaoom.monitor.OOMReportUploader;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import kotlin.jvm.functions.Function0;

/**
 *
 * https://github.com/KwaiAppTeam/KOOM/blob/master/koom-java-leak/README.zh-CN.md
 * 2021-10-22 10:20:50.281 8123-8212/com.hss01248.flipperdemo I/OOMMonitor_SystemInfo: [java] max:268435456 used ratio:26%
 * 2021-10-22 10:20:50.281 8123-8212/com.hss01248.flipperdemo I/OOMMonitor_SystemInfo: [proc] VmSize:5930628kB VmRss:325176kB Threads:42
 * 2021-10-22 10:20:50.282 8123-8212/com.hss01248.flipperdemo I/OOMMonitor_SystemInfo: [meminfo] MemTotal:7750540kB MemFree:78112kB MemAvailable:2773816kB
 * 2021-10-22 10:20:50.282 8123-8212/com.hss01248.flipperdemo I/OOMMonitor_SystemInfo: avaliable ratio:35% CmaTotal:253952kB ION_heap:0kB
 */
public class LeakDetector {

     static void init(Application application){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return;
        }
        CommonConfig commonConfig = new CommonConfig.Builder()
                .setApplication(application)
                .setDebugMode(true)
                .setVersionNameInvoker(new Function0<String>() {
                    @Override
                    public String invoke() {
                        return "1.0.0";
                    }
                })
                .build();

        MonitorManager.initCommonConfig(commonConfig);
        OOMMonitorConfig config = new OOMMonitorConfig.Builder().setEnableHprofDumpAnalysis(true)
                .setReportUploader(new OOMReportUploader() {
                    @Override
                    public void upload(@NotNull File file, @NotNull String content) {
                        Log.i("OOMMonitor", content);
                        Log.e("OOMMonitor", "todo, upload Report  if necessary "+file.getAbsolutePath());
                    }
                })
                .setHprofUploader(new OOMHprofUploader() {
                    @Override
                    public void upload(@NotNull File file, @NotNull HprofType hprofType) {
                        Log.e("OOMMonitor", "todo, upload hprof  if necessary "+file.getAbsolutePath());
                    }
                }).build();
        MonitorManager.addMonitorConfig(config);
        MonitorManager.onApplicationCreate();
        OOMMonitor.INSTANCE.init(commonConfig ,config);

       OOMMonitor.INSTANCE.startLoop(true,true,5_000L);

    }
}
