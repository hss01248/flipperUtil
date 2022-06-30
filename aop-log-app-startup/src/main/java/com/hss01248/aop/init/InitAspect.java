package com.hss01248.aop.init;

import com.hss01248.logforaop.LogMethodAspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * by hss
 * data:2020/7/17
 * desc:
 */
@Aspect
public class InitAspect {

    private static final String TAG = "InitAspect";


    @Before("execution(* androidx.startup.Initializer.create(..))")
    public void weaveJoinPoint(JoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(true, TAG, joinPoint, new LogMethodAspect.IBefore() {
            @Override
            public void before(JoinPoint joinPoin, String desc) {
                LogMethodAspect.IBefore.super.before(joinPoin, desc);
            }
        });
    }


}
