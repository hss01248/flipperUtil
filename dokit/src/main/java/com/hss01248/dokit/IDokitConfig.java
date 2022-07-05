package com.hss01248.dokit;

import android.content.Context;

public interface IDokitConfig {

    void loadUrl(Context context, String url);

    void report(Object o);

    default void goTestPage(){

    }

    default String getExtraInitClassName(){
        return "";

    }

    default String getUserInfo(){
        return "未实现";
    }

    /**
    TraceInfo.create("method_block_main_thread_167")
    *           .setMainMetric(costTime)
    *                                         //.addMetirc("blockTime_ms",costTime)
    *          .addAttribute("methodDesc",desc+"_"+stackTraceElements1[0].getLineNumber())
    *           .report();
     * @param blockTimeMs
     * @param thresholdTimeMs
     * @param methodDesc
     * @param stack
     */
    default void onMainBlock(long blockTimeMs,long thresholdTimeMs,String methodDesc,Throwable stack){}
}
