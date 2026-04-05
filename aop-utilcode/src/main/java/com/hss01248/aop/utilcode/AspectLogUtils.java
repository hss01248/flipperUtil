package com.hss01248.aop.utilcode;


import com.blankj.utilcode.util.LogUtils;
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;

import java.util.ArrayList;
import java.util.List;

public class AspectLogUtils {

    public static void setReport(ILogReport report) {
        AspectLogUtils.report = report;
    }

    static ILogReport report;

    public static Object interceptLog(ProceedJoinPoint joinPoint) throws Throwable {
        int type = (int) joinPoint.getArgs()[0];
        if (type < LogUtils.W) {
            return joinPoint.proceed();
        }
        Object[] objects = (Object[]) joinPoint.getArgs()[2];
        if (objects == null || objects.length == 0) {
            return joinPoint.proceed();
        }
        boolean hasThrowable = false;
        Throwable throwable = null;
        String msg = "";
        List<Object> list = new ArrayList<>();
        for (Object object : objects) {
            if (object instanceof Throwable) {
                hasThrowable = true;
                throwable = (Throwable) object;
            } else {
                list.add(object);
            }
        }
        String typeStr = "warn";
        if (type == LogUtils.E) {
            typeStr = "error";
        } else if (type == LogUtils.A) {
            typeStr = "assert";
        } else if (type == LogUtils.W) {
            typeStr = "warn";
        } else {
            typeStr = type + "";
        }
        if (!hasThrowable) {
            msg = objects[0] + "";
        }
        String tag = joinPoint.getArgs()[1] + "";
        if (report != null) {
            report.report(typeStr, tag, msg, throwable, list.toArray());
        }
        return joinPoint.proceed();
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "com.blankj.utilcode.util.LogUtils",
        methodName = {"log"},
        type = MatchType.SELF
)
class LogUtilsLogMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return AspectLogUtils.interceptLog(joinPoint);
    }
}
