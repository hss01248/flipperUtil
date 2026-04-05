package com.hss01248.flipper.aop.jsRNbridge;


import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.hss01248.logforaop.LogMethodAspect;


/**
 * 已废弃占位类；EventBus 织入见 flipper 模块 {@link com.hss01248.flipper.eventbus.EventBusAspect}。
 */
@Deprecated
public class EventBusAspect {

    private static final String TAG = "busAspect";

    public void weaveJoinPoint(ProceedJoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public String descExtraForLog() {
                return "";
            }

            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {
            }
        });
    }


}
