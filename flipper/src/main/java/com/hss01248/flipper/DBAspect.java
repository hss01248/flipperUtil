package com.hss01248.flipper;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * by hss
 * data:2020/7/17
 * desc:
 */
@Aspect
public class DBAspect {

    private static final String TAG = "DBAspect";
    static int count;



    static List<String> outDbFiles = new ArrayList<>();

    public static void addDB(File dbFile){
        if(dbFile == null){
            return;
        }
      if(!outDbFiles.contains(dbFile.getAbsolutePath())){
          outDbFiles.add(dbFile.getAbsolutePath());
      }
    }

    @Around("execution(* com.facebook.flipper.plugins.databases.impl.DefaultSqliteDatabaseProvider.getDatabaseFiles(..))")
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
             result = joinPoint.proceed();
             if(result instanceof List){
                 List datas = (List) result;
                 //避免重复添加
                 List<File> files = new ArrayList<>();
                 for (String outDbFile : outDbFiles) {
                     boolean contains = false;
                     for (Object data : datas) {
                         if(data instanceof File){
                             File file = (File) data;
                             if(file.getAbsolutePath().equals(outDbFile)){
                                 contains = true;
                                 break;
                             }
                         }
                     }
                     if(!contains){
                         files.add(new File(outDbFile));
                     }
                 }
                 if(files.size() > 0){

                     datas.add(files);
                     Log.v(TAG,"添加n个外部数据库:"+files.size() );
                 }
                 Log.v(TAG,"数据库总个数:"+datas.size() );
             }

        }catch (Throwable throwable){
            throwable.printStackTrace();
        }
        long duration = System.currentTimeMillis() - begin;
        Log.v(TAG,joinPoint.getThis()+"."+methodName+"  耗时:"+duration+"ms" );

        return result;
    }


}
