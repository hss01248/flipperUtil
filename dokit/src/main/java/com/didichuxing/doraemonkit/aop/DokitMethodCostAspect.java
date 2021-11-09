package com.didichuxing.doraemonkit.aop;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class DokitMethodCostAspect {

    @Around("execution(* com.didichuxing.doraemonkit.aop.MethodCostUtil.recodeObjectMethodCostEnd(..))")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {

    }
}
