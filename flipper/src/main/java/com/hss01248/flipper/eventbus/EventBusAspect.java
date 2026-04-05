package com.hss01248.flipper.eventbus;

import com.blankj.utilcode.util.LogUtils;
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;

/**
 * EventBus 调用日志（AndroidAOP）。
 */
public class EventBusAspect {

    private static final String TAG = "busAspect";

    static Object interceptEventBus(ProceedJoinPoint joinPoint) throws Throwable {
        LogUtils.d("bus", joinPoint);
        EventBusLogger2FlipperPlugin.sendData(joinPoint.getArgs()[0], joinPoint.getTargetMethod().getName());
        return joinPoint.proceed();
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "org.greenrobot.eventbus.EventBus",
        methodName = {"post", "removeStickyEvent"},
        type = MatchType.SELF
)
class EventBusPostMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return EventBusAspect.interceptEventBus(joinPoint);
    }
}
