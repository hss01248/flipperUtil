package com.hss01248.aop.network.hook;

import android.util.Log;

import com.blankj.utilcode.util.LogUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

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
        Collections.sort(hooks, new Comparator<OkhttpHook>() {
            @Override
            public int compare(OkhttpHook o1, OkhttpHook o2) {
                return o2.initOrder() - o1.initOrder();
            }
        });
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
                        Iterator<OkhttpHook> iterator = hooks.iterator();
                        LogUtils.dTag(TAG,hooks );
                        while (iterator.hasNext()){
                            iterator.next().beforeBuild(builder);
                        }
                        LogUtils.iTag(TAG,builder.interceptors() );
                        LogUtils.dTag(TAG,builder.networkInterceptors() );
                    }
                }
                count++;
            }else {
                Log.v(TAG,"is RN dev socket connector!!! ignore ");
            }

             result = joinPoint.proceed();
            if(elements != null && elements.length>0 &&  result instanceof OkHttpClient){
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
                    .append(":\t")
                    .append(clientInfo(client))
                    .append("\n");
        }
        return builder.toString();
    }

    private String clientInfo(OkHttpClient client) {
        StringBuilder sb = new StringBuilder();
        ExecutorService executorService =  client.dispatcher().executorService();
        if(executorService instanceof ThreadPoolExecutor){
            ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
            sb.append("activeThread:")
                    .append(executor.getActiveCount())
                    .append(", coreSize:")
                    .append(executor.getCorePoolSize())
                    .append(", poolSize:")
                    .append(executor.getPoolSize())
                    .append(", taskCount:")
                    .append(executor.getTaskCount())
                    .append(", CompletedTaskCount:")
                    .append(executor.getCompletedTaskCount());
        }else {
            sb.append(executorService);
        }
        return sb.toString();
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

        default  int initOrder(){
            return 0;
        }
    }
}
