package com.hss01248.aop.init;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;
import com.hss01248.logforaop.LogMethodAspect;

/**
 * RxJava 调用日志（AndroidAOP）。覆盖常用算子；未列出的方法名不会打日志。
 */
public class RxAspect {

    static Object interceptRx(ProceedJoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(true, "RxAspect", joinPoint, new LogMethodAspect.IBefore() {
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

    static Object interceptFunctionApply(ProceedJoinPoint joinPoint) throws Throwable {
        return interceptRx(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.Observable",
        methodName = {
                "subscribe", "subscribeActual", "subscribeWith", "observeOn", "subscribeOn",
                "map", "flatMap", "concatMap", "switchMap", "filter", "take", "compose",
                "create", "just", "fromIterable", "fromArray", "fromCallable", "defer",
                "merge", "zip", "interval", "timer", "error", "empty", "never", "range",
                "replay", "publish", "cache", "share", "hide", "cast", "ofType",
                "first", "firstOrError", "single", "last", "elementAt", "distinct",
                "debounce", "buffer", "scan", "reduce", "collect", "toList", "count",
                "retry", "repeat", "delay", "timeout", "doOnEach", "doOnNext", "doOnSubscribe",
                "blockingFirst", "blockingSubscribe", "test"
        },
        type = MatchType.SELF
)
class RxObservableMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptRx(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.Observer",
        methodName = {"onSubscribe", "onNext", "onError", "onComplete"},
        type = MatchType.EXTENDS
)
class RxObserverMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptRx(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.functions.Function",
        methodName = {"apply"},
        type = MatchType.EXTENDS
)
class RxFunctionMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptFunctionApply(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.functions.BiFunction",
        methodName = {"apply"},
        type = MatchType.EXTENDS
)
class RxBiFunctionMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptFunctionApply(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.functions.Function3",
        methodName = {"apply"},
        type = MatchType.EXTENDS
)
class RxFunction3Match implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptFunctionApply(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.functions.Function4",
        methodName = {"apply"},
        type = MatchType.EXTENDS
)
class RxFunction4Match implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptFunctionApply(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.functions.Function5",
        methodName = {"apply"},
        type = MatchType.EXTENDS
)
class RxFunction5Match implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptFunctionApply(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.functions.Function6",
        methodName = {"apply"},
        type = MatchType.EXTENDS
)
class RxFunction6Match implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptFunctionApply(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.functions.Function7",
        methodName = {"apply"},
        type = MatchType.EXTENDS
)
class RxFunction7Match implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptFunctionApply(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.functions.Function8",
        methodName = {"apply"},
        type = MatchType.EXTENDS
)
class RxFunction8Match implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptFunctionApply(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "io.reactivex.functions.Function9",
        methodName = {"apply"},
        type = MatchType.EXTENDS
)
class RxFunction9Match implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return RxAspect.interceptFunctionApply(joinPoint);
    }
}
