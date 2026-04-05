package com.hss01248.aop.init;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;
import com.hss01248.logforaop.LogMethodAspect;

/**
 * LiveData / ViewModel 日志（AndroidAOP）。
 */
public class JetpackAspect {

    private static final String TAG = "jetAspect";

    static Object interceptLiveData(ProceedJoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public String descExtraForLog() {
                return "";
            }

            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {
            }
        });
        return joinPoint.proceed();
    }

    static Object interceptViewModelProviderGet(ProceedJoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public String descExtraForLog() {
                return "";
            }

            @Override
            public void before(ProceedJoinPoint joinPoin, String desc) {
            }
        });
        return joinPoint.proceed();
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "androidx.lifecycle.LiveData",
        methodName = {"observe", "postValue", "setValue"},
        type = MatchType.EXTENDS
)
class LiveDataLifecycleMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return JetpackAspect.interceptLiveData(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "androidx.lifecycle.ViewModelProvider",
        methodName = {"get"},
        type = MatchType.SELF
)
class ViewModelProviderGetMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return JetpackAspect.interceptViewModelProviderGet(joinPoint);
    }
}
