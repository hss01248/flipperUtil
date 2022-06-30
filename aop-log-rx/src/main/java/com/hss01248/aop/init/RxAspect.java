package com.hss01248.aop.init;

import com.hss01248.logforaop.LogMethodAspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/**
 * @Despciption todo
 * @Author hss
 * @Date 12/04/2022 14:21
 * @Version 1.0
 */
@Aspect
public class RxAspect {


    @Before("execution(* io.reactivex.Observable.*(..)) || execution(* io.reactivex.functions.Function*.*(..)) " +
            "|| execution(* io.reactivex.Observer.*(..))")
    public void weaveJoinPoint(JoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(true,"RxAspect",joinPoint,new LogMethodAspect.IBefore(){
            @Override
            public String descExtraForLog(){
                return "";
            }
        });
    }

}
