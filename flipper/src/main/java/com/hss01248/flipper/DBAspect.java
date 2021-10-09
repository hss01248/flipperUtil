package com.hss01248.flipper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
    static List<String> outDbFiles = new ArrayList<>();
    static boolean parsed = false;

    public static void addDB(File dbFile){
        if(dbFile == null){
            return;
        }
      if(!outDbFiles.contains(dbFile.getAbsolutePath())){
          outDbFiles.add(dbFile.getAbsolutePath());
      }

      FlipperUtil.context.getSharedPreferences("flipper", Context.MODE_PRIVATE).edit().putString("db",new Gson().toJson(outDbFiles)).apply();


    }

    @Around("execution(* com.facebook.flipper.plugins.databases.impl.DefaultSqliteDatabaseProvider.getDatabaseFiles(..))")
    public Object weaveJoinPoint(ProceedingJoinPoint joinPoint) throws Throwable {
        if(!parsed){
            parsed = true;
           String s =  FlipperUtil.context.getSharedPreferences("flipper", Context.MODE_PRIVATE).getString("db","");
           if(!TextUtils.isEmpty(s)){
               try {
                   outDbFiles = new Gson().fromJson(s,new TypeToken<List<String>>(){}.getType());
               }catch (Throwable throwable){
                   throwable.printStackTrace();
               }
           }
        }

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
                     datas.addAll(files);
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
