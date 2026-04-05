package com.didichuxing.doraemonkit.aop;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;

/**
 * 慢函数统计桥接（AndroidAOP）。
 */
public final class DokitMethodCostAspect {

    private DokitMethodCostAspect() {
    }

    static Object interceptStaticMethodCostEnd(ProceedJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 2) {
            MethodCostUtilImpl.INSTANCE.recodeStaticMethodCostEnd((int) args[0], (String) args[1]);
        }
        return null;
    }

    static Object interceptStaticMethodCostStart(ProceedJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length == 2) {
            MethodCostUtilImpl.INSTANCE.recodeStaticMethodCostStart((int) args[0], (String) args[1]);
        }
        return null;
    }

    static Object interceptNoop(ProceedJoinPoint joinPoint) {
        return null;
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "com.didichuxing.doraemonkit.aop.MethodCostUtil",
        methodName = {"recodeStaticMethodCostEnd"},
        type = MatchType.SELF
)
class DokitRecodeStaticMethodCostEndMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return DokitMethodCostAspect.interceptStaticMethodCostEnd(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "com.didichuxing.doraemonkit.aop.MethodCostUtil",
        methodName = {"recodeStaticMethodCostStart"},
        type = MatchType.SELF
)
class DokitRecodeStaticMethodCostStartMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return DokitMethodCostAspect.interceptStaticMethodCostStart(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "com.didichuxing.doraemonkit.aop.MethodCostUtil",
        methodName = {"recodeObjectMethodCostEnd"},
        type = MatchType.SELF
)
class DokitRecodeObjectMethodCostEndMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return DokitMethodCostAspect.interceptNoop(joinPoint);
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "com.didichuxing.doraemonkit.aop.MethodCostUtil",
        methodName = {"recodeObjectMethodCostStart"},
        type = MatchType.SELF
)
class DokitRecodeObjectMethodCostStartMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return DokitMethodCostAspect.interceptNoop(joinPoint);
    }
}
