package com.hss01248.aop.init;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.base.BasePointCut;
import com.hss01248.logforaop.LogMethodAspect;

/**
 * {@link AopJavascriptInterface} 切面实现。
 */
public class JsObjCut implements BasePointCut<AopJavascriptInterface> {

    @Override
    public Object invoke(ProceedJoinPoint joinPoint, AopJavascriptInterface annotation) throws Throwable {
        return LogMethodAspect.logAround(JsObjAspect.enableLog, JsObjAspect.TAG, false, joinPoint, new LogMethodAspect.IAround() {
            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {
                JsObjAspect.printLogToJsConsole(joinPoin, desc);
            }

            @Override
            public String descExtraForLog() {
                if (JsObjAspect.getUrl != null) {
                    return "url:" + JsObjAspect.getUrl.getUrl();
                }
                return "please set JsObjAspect.getUrl";
            }
        });
    }
}
