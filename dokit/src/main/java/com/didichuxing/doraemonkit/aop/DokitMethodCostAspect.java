package com.didichuxing.doraemonkit.aop;


import com.didichuxing.doraemonkit.util.LogUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class DokitMethodCostAspect {

    @Around("execution(* com.didichuxing.doraemonkit.aop.MethodCostUtil.recodeObjectMethodCostEnd(..))")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        //MethodCostUtil.INSTANCE.recodeObjectMethodCostEnd();
        LogUtils.d("MethodCostUtil.recodeObjectMethodCostEnd  ->");
        Object[] args = joinPoint.getArgs();
        if(args.length == 2){
            MethodCostUtilImpl.INSTANCE.recodeStaticMethodCostEnd((int)args[0],(String)args[1]);
        }

        //Integer
        return null;
    }
    @Around("execution(* com.didichuxing.doraemonkit.aop.MethodCostUtil.recodeStaticMethodCostStart(..))")
    public Object weaveJoinPoint2(ProceedingJoinPoint joinPoint) throws Throwable {
       // MethodCostUtil.INSTANCE.recodeStaticMethodCostStart();
        LogUtils.d("MethodCostUtil.recodeStaticMethodCostStart  ->");
        Object[] args = joinPoint.getArgs();
        if(args.length == 2){
            MethodCostUtilImpl.INSTANCE.recodeStaticMethodCostStart((int)args[0],(String)args[1]);
        }

        //Integer
        return null;
    }
}
