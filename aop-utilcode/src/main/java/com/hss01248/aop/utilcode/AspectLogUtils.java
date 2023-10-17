package com.hss01248.aop.utilcode;


import com.blankj.utilcode.util.LogUtils;
import com.hss01248.sentry.SentryUtil;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class AspectLogUtils {

    /**
     * log(final int type, final String tag, final Object... contents)
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Before("execution(* com.blankj.utilcode.util.LogUtils.log(..))")
    public void weaveJoinPoint(JoinPoint joinPoint) throws Throwable {
        int type = (int) joinPoint.getArgs()[0];
        if(type < LogUtils.W){
            return;
        }
        Object[] objects= (Object[]) joinPoint.getArgs()[2];
        if(objects  == null || objects.length ==0){
            return;
        }
        SentryUtil.Builder builder = SentryUtil.create();
        boolean hasThrowable = false;
        int count = 0;
        for (Object object : objects) {
            if(object instanceof  Throwable){
                hasThrowable = true;
                Throwable throwable = (Throwable) object;
                builder.exception(throwable);
            }else {
                count++;
                builder.addExtra("extra"+count,object+"");
            }
        }
        String typeStr = "warn";
        if(type == LogUtils.E){
            typeStr = "error";
        }else if(type == LogUtils.A){
            typeStr = "assert";
        }else if(type == LogUtils.W){
            typeStr = "warn";
        }else {
            typeStr = type+"";
        }
        builder.addTag("logLevel",typeStr);
        if(hasThrowable){
           // builder.addExtra("extraMsg",joinPoint.getArgs()[0]+"");
        }else {
            builder.msg(objects[0]+"");
        }
        String tag = joinPoint.getArgs()[1]+"";
        if(!tag.equals("") && !"null".equals(tag)){
            builder.addTag("extraTag",joinPoint.getArgs()[1]+"");
        }
        builder.doReport();
    }
}
