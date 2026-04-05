package com.hss01248.aop.init;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;
import com.hss01248.logforaop.LogMethodAspect;

/**
 * App Startup Initializer 日志（AndroidAOP）。
 */
public class InitAspect {

    private static final String TAG = "InitAspect";

    static Object interceptCreate(ProceedJoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {
            }
        });
        return joinPoint.proceed();
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "androidx.startup.Initializer",
        methodName = {"create"},
        type = MatchType.EXTENDS
)
class InitializerCreateMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return InitAspect.interceptCreate(joinPoint);
    }
}
