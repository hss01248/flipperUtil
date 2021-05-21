package com.hss01248.flipper;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * by hss
 * data:2020/7/17
 * desc:
 */
@Aspect
public class OkhttpAspect {

    private static final String TAG = "OkhttpAspect";



    static List<OkhttpHook> hooks = new ArrayList<>();

    public static void addHook(OkhttpHook hook){
        hooks.add(hook);
    }

    @Around("execution(* okhttp3.OkHttpClient.Builder.build(..))")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = methodSignature.getDeclaringType().getSimpleName();
        String methodName = methodSignature.getName();
        //String funName = methodSignature.getMethod().getAnnotation(TimeSpend.class).value();
        Log.v(TAG,"method begin:"+methodName );
        //统计时间
        long begin = System.currentTimeMillis();
        Object result = null;
        try {
            if(!isRN()){
                if(joinPoint.getThis() instanceof OkHttpClient.Builder){
                    OkHttpClient.Builder builder = (OkHttpClient.Builder) joinPoint.getThis();
                    if(hooks.size() > 0){
                        for (int i = 0; i < hooks.size(); i++) {
                            hooks.get(i).beforeBuild(builder);
                        }
                    }
                }
            }else {
                Log.v(TAG,"is fucking RN dev socket connector!!! ignore ");
            }

             result = joinPoint.proceed();
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        long duration = System.currentTimeMillis() - begin;
        Log.v(TAG,joinPoint.getArgs()+"."+methodName+"  耗时:"+duration+"ms" );

        return result;
    }

    private boolean isRN() {
        Exception exception = new Exception();
        Log.e(TAG,"clientBuilder.build() call stacks",exception);
        StackTraceElement[] stackTraces = exception.getStackTrace();
        for (StackTraceElement stackTrace : stackTraces) {
            if(stackTrace.getClassName().contains("com.facebook.react")){
                return true;
            }
        }
        return false;
    }

    public  interface OkhttpHook{
        void beforeBuild(OkHttpClient.Builder builder);
    }
}
