package com.didichuxing.doraemonkit.aop;



import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class DokitMethodCostAspect {

    @Around("execution(* com.didichuxing.doraemonkit.aop.MethodCostUtil.recodeStaticMethodCostEnd(..))")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        //LogUtils.d("MethodCostUtil.recodeObjectMethodCostEnd  ->");
        Object[] args = joinPoint.getArgs();
        if(args.length == 2){
            //LogUtils.d("MethodCostUtil.recodeObjectMethodCostEnd  ->");
            MethodCostUtilImpl.INSTANCE.recodeStaticMethodCostEnd((int)args[0],(String)args[1]);
        }

        //Integer
        return null;
    }
    @Around("execution(* com.didichuxing.doraemonkit.aop.MethodCostUtil.recodeStaticMethodCostStart(..))")
    public Object weaveJoinPoint2(ProceedingJoinPoint joinPoint) throws Throwable {
       // MethodCostUtil.INSTANCE.recodeStaticMethodCostStart();

        Object[] args = joinPoint.getArgs();
        if(args.length == 2){
            //LogUtils.d("MethodCostUtil.recodeStaticMethodCostStart  ->");
            MethodCostUtilImpl.INSTANCE.recodeStaticMethodCostStart((int)args[0],(String)args[1]);
        }

        //Integer
        return null;
    }

    @Around("execution(* com.didichuxing.doraemonkit.aop.MethodCostUtil.recodeObjectMethodCostEnd(..))")
    public Object weaveJoinPoint4(ProceedingJoinPoint joinPoint) throws Throwable {
        //空实现,避免重复
        return null;
    }
    @Around("execution(* com.didichuxing.doraemonkit.aop.MethodCostUtil.recodeObjectMethodCostStart(..))")
    public Object weaveJoinPoint5(ProceedingJoinPoint joinPoint) throws Throwable {
        //空实现,避免重复
        return null;
    }
}
