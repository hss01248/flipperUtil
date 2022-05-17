package com.hss01248.flipper;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.OkHttpClient;

/**
 * by hss
 * data:2020/7/17
 * desc:
 */
@Aspect
public class OkhttpAspect {

    private static final String TAG = "OkhttpAspect";
    static int count;



    static List<OkhttpHook> hooks = new ArrayList<>();

    public static void addHook(OkhttpHook hook){
        hooks.add(hook);
    }


    static WeakHashMap<OkHttpClient,String> clientMap = new WeakHashMap<>();
    //static CopyOnWriteArrayList<WeakReference<OkHttpClient>> clients = new CopyOnWriteArrayList<>();

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
            StackTraceElement[] elements = isRN();
            if(elements != null){//不是rn
                if(joinPoint.getThis() instanceof OkHttpClient.Builder){
                    OkHttpClient.Builder builder = (OkHttpClient.Builder) joinPoint.getThis();
                    if(hooks.size() > 0){
                        for (int i = 0; i < hooks.size(); i++) {
                            hooks.get(i).beforeBuild(builder);
                        }
                    }
                }
                count++;
            }else {
                Log.v(TAG,"is RN dev socket connector!!! ignore ");
            }

             result = joinPoint.proceed();
            if(result instanceof OkHttpClient){
                OkHttpClient client = (OkHttpClient) result;
                //clients.add(new WeakReference<>(client));
                clientMap.put(client,elements[0].toString());
            }

            long duration = System.currentTimeMillis() - begin;
            Log.v(TAG,joinPoint.getThis()+"."+methodName+"  耗时:"+duration+"ms,已构建常规okhttpclient个数:"+count );
            Log.d(TAG,"okhttpClient信息:\n"+clientsInfo2());

        }catch (Throwable throwable){
            Log.w(TAG,"构建okhttpclient失败",throwable);
        }


        return result;
    }

    private String clientsInfo2() {
        StringBuilder builder = new StringBuilder();
        builder.append("client real count: ")
                .append(clientMap.size())
                .append("\n");
        for (OkHttpClient client : clientMap.keySet()) {
            builder.append(client)
                    .append(":\t")
                    .append(clientMap.get(client))
                    .append("\n");
        }
        return builder.toString();
    }

    /*private String clientsInfo() {
        StringBuilder builder = new StringBuilder();
        Iterator<WeakReference<OkHttpClient>> iterator = clients.iterator();
        int count = 0;
        while (iterator.hasNext()){
            WeakReference<OkHttpClient> next = iterator.next();
            if(next.get() == null){
                iterator.remove();
            }else {
                OkHttpClient client = next.get();
                builder.append(count)
                        .append(":\t")
                        .append(info(client))
                .append("\n");

                count ++;
            }
        }
        builder.insert(0,"realcount:"+count+"\n");
        return builder.toString();
    }*/

    private String info(OkHttpClient client) {
        StringBuilder builder = new StringBuilder();
        builder.append(Arrays.toString(client.interceptors().toArray()))
                .append("\n");
                //.append()
        client.dispatcher().executorService().submit(new Runnable() {
            @Override
            public void run() {
                Log.v(TAG,"mainUrl:"+Thread.currentThread().getName());
            }
        });
        return builder.toString();
    }

    private StackTraceElement[] isRN() {
        Exception exception = new Exception("just show okhttpclient build stacks");

        StackTraceElement[] stackTraces = exception.getStackTrace();
        for (StackTraceElement stackTrace : stackTraces) {
            if(stackTrace.getClassName().contains("com.facebook.react.packagerconnection")){
                return null;
            }
            if(stackTrace.getClassName().contains("com.facebook.react.devsupport")){
                return null;
            }
        }
        StackTraceElement[] stackTraceElements1 = new StackTraceElement[stackTraces.length-4];
        for (int i = 0; i < stackTraces.length-4; i++) {
            stackTraceElements1[i] = stackTraces[i+4];
        }
        exception.setStackTrace(stackTraceElements1);
        Log.v(TAG,"clientBuilder.build() call stacks",exception);
        return stackTraceElements1;
    }

    public  interface OkhttpHook{
        /**
         * 因为有些情况下会调用client.newBuilder().builder(),如果加拦截器,要自行判重
         * @param builder
         */
        void beforeBuild(OkHttpClient.Builder builder);
    }
}
