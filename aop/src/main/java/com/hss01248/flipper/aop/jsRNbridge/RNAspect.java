package com.hss01248.flipper.aop.jsRNbridge;

import android.util.Log;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;
import com.hss01248.logforaop.LogMethodAspect;

/**
 * React Native 桥接日志（AndroidAOP）。
 * 原 AspectJ 中含 @ReactMethod 的切点需改为在方法上增加日志；此处覆盖 Promise / Callback / RCTDeviceEventEmitter.emit。
 */
public class RNAspect {

    private static final String TAG = "RNAspect";

    static Object interceptRn(ProceedJoinPoint joinPoint) throws Throwable {
        Object target = joinPoint.getTarget();
        if (target != null && "com.facebook.react.uimanager.UIManagerModule".equals(target.getClass().getName())) {
            return joinPoint.proceed();
        }
        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public String descExtraForLog() {
                return "";
            }

            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {
                Log.d("ReactNativeJS-an", desc);
            }
        });
        return joinPoint.proceed();
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "com.facebook.react.bridge.Promise",
        methodName = {"resolve", "reject"},
        type = MatchType.EXTENDS
)
class RnPromiseMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RNAspect.interceptRn(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "com.facebook.react.bridge.Callback",
        methodName = {"invoke"},
        type = MatchType.EXTENDS
)
class RnCallbackMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RNAspect.interceptRn(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "com.facebook.react.modules.core.DeviceEventManagerModule$RCTDeviceEventEmitter",
        methodName = {"emit"},
        type = MatchType.SELF
)
class RnDeviceEventEmitterMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RNAspect.interceptRn(joinPoint);
    }
}
