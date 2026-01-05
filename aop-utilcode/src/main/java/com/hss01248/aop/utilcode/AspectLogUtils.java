package com.hss01248.aop.utilcode;


import com.blankj.utilcode.util.LogUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import java.util.ArrayList;
import java.util.List;

@Aspect
public class AspectLogUtils {


    public static void setReport(ILogReport report) {
        AspectLogUtils.report = report;
    }

     static ILogReport report;
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
       // SentryUtil.Builder builder = SentryUtil.create();
        boolean hasThrowable = false;
        Throwable throwable = null;
        String msg = "";
        int count = 0;
        List<Object> list = new ArrayList<>();
        for (Object object : objects) {
            if(object instanceof  Throwable){
                hasThrowable = true;
                 throwable = (Throwable) object;
                //builder.exception(throwable);
            }else {
                count++;
               // builder.addExtra("extra"+count,object+"");
                list.add(object);
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
       // builder.addTag("logLevel",typeStr);
        if(hasThrowable){
           // builder.addExtra("extraMsg",joinPoint.getArgs()[0]+"");
        }else {
            msg = objects[0]+"";
           // builder.msg(objects[0]+"");
        }
        String tag = joinPoint.getArgs()[1]+"";
        if(!tag.equals("") && !"null".equals(tag)){
           // builder.addTag("extraTag",joinPoint.getArgs()[1]+"");
        }
       // builder.doReport();
        if(report == null){

        }else {
            //如果传入的是一个 普通对象（包括 ArrayList），编译器会自动创建一个长度为 1 的数组，并将这个对象放入其中。
            report.report(typeStr,tag,msg,throwable,list.toArray());
        }

    }
}
