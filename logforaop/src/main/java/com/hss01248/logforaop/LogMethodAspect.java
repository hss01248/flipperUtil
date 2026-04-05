package com.hss01248.logforaop;

import com.flyjingfish.android_aop_annotation.AopMethod;
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;

import java.util.Arrays;

/**
 * 基于 AndroidAOP {@link ProceedJoinPoint} 的日志辅助（替代原依赖 AspectJ 的 logforaop 库）。
 */
public final class LogMethodAspect {

    private LogMethodAspect() {
    }

    public interface IBefore {
        void before(ProceedJoinPoint joinPoint, String desc);

        default String descExtraForLog() {
            return "";
        }
    }

    public interface IAround {
        void before(ProceedJoinPoint joinPoint, String desc);

        String descExtraForLog();
    }

    public static void logBefore(boolean enable, String tag, ProceedJoinPoint joinPoint, IBefore before) {
        if (!enable || before == null) {
            return;
        }
        String extra = "";
        try {
            extra = before.descExtraForLog();
            if (extra == null) {
                extra = "";
            }
        } catch (Throwable ignored) {
        }
        String desc = extra + buildDesc(joinPoint);
        try {
            before.before(joinPoint, desc);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static Object logAround(boolean enable, String tag, boolean unused, ProceedJoinPoint joinPoint, IAround around) throws Throwable {
        if (!enable || around == null) {
            return joinPoint.proceed();
        }
        String extra = around.descExtraForLog();
        if (extra == null) {
            extra = "";
        }
        String desc = extra + buildDesc(joinPoint);
        around.before(joinPoint, desc);
        return joinPoint.proceed();
    }

    public static String buildDesc(ProceedJoinPoint joinPoint) {
        try {
            AopMethod m = joinPoint.getTargetMethod();
            String shorty = m.getDeclaringClass().getName() + "." + m.getName() + "(..)";
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                return shorty.replace("..", Arrays.toString(args));
            }
            return shorty;
        } catch (Throwable t) {
            return String.valueOf(joinPoint);
        }
    }
}
