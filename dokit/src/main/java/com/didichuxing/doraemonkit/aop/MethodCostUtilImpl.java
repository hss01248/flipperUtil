package com.didichuxing.doraemonkit.aop;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.util.Log;


import com.didichuxing.doraemonkit.aop.method_stack.StaticMethodObject;
import com.didichuxing.doraemonkit.kit.timecounter.TimeCounterManager;
import com.didichuxing.doraemonkit.util.ThreadUtils;
import com.hss01248.dokit.MyDokit;

import java.util.concurrent.ConcurrentHashMap;


public class MethodCostUtilImpl {

    public static String TAG = "DOKIT_SLOW_METHOD2";

    private static ConcurrentHashMap<String, Long> METHOD_COSTS = new ConcurrentHashMap<>();

    private static StaticMethodObject staticMethodObject = new  StaticMethodObject();

    public static int MIN_TIME = 80;//MS


    public static MethodCostUtilImpl INSTANCE = new MethodCostUtilImpl();

    public synchronized    void  recodeObjectMethodCostStart( int thresholdTime,String methodName, Object classObj) {
        try {
            METHOD_COSTS.put(methodName,System.currentTimeMillis()) ;
            if (classObj instanceof Application) {
                String[] methods = methodName.split("&");
                if (methods.length == 2) {
                    if (methods[1] == "onCreate") {
                        TimeCounterManager.get().onAppCreateStart();
                    }
                    if (methods[1] == "attachBaseContext") {
                        TimeCounterManager.get().onAppAttachBaseContextStart();
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public  void  recodeStaticMethodCostStart(int thresholdTime,String methodName) {
        recodeObjectMethodCostStart(thresholdTime, methodName, staticMethodObject);

    }

    /**
     * 对象方法
     *
     * @param thresholdTime 预设的值 单位为us 1000us = 1ms
     * @param methodName
     * @param classObj      调用该函数的对象
     */
    public  void  recodeObjectMethodCostEnd(int thresholdTime,String methodName, Object classObj) {
        synchronized(MethodCostUtilImpl.class) {
            try {
                if (METHOD_COSTS.containsKey(methodName)) {
                    Long startTime = METHOD_COSTS.get(methodName);
                    if(startTime == null){
                        return;
                    }
                            long costTime = (System.currentTimeMillis() - startTime);
                    METHOD_COSTS.remove(methodName);
                    if (classObj instanceof Application) {
                        //Application 启动时间统计
                        String[] methods = methodName.split("&");
                        if (methods.length == 2) {
                            if (methods[1] == "onCreate") {
                                TimeCounterManager.get().onAppCreateEnd();
                            }
                            if (methods[1] == "attachBaseContext") {
                                TimeCounterManager.get().onAppAttachBaseContextEnd();
                            }
                        }
                        //printApplicationStartTime(methodName);
                    } else if (classObj instanceof Activity) {
                        //Activity 启动时间统计
                        //printActivityStartTime(methodName);
                    } else if (classObj instanceof Service) {
                        //service 启动时间统计
                    }

                    //如果该方法的执行时间大于1ms 则记录
                    thresholdTime = MIN_TIME;
                    if (costTime >= thresholdTime) {
                        String threadName = Thread.currentThread().getName();
                        if(!"main".equals(threadName)){
                            Log.i(TAG, "================not main method, do not report================ "+methodName +"(), cost "+costTime);
                            return;
                        }
                        Log.i(TAG, "================Dokit================");
                        //String msg = "methodName===>"+methodName+"  threadName==>"+threadName+"  thresholdTime===>"+thresholdTime+"   costTime===>"+costTime;
                        methodName = methodName.replace("&",".");
                        methodName = methodName+"()";
                        String msg2 = methodName+"   cost(ms)====>"+costTime;
                        Log.i(TAG, "\t "+msg2);
                        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                        //上报到bugly

                        //减少无用层级
                        StackTraceElement[] stackTraceElements1 = new StackTraceElement[stackTraceElements.length-6];
                        for (int i = 0; i < stackTraceElements.length-6; i++) {
                            stackTraceElements1[i] = stackTraceElements[i+6];
                            Log.i(TAG, "\tat "+stackTraceElements1[i]);
                        }

                        ThreadUtils.getIoPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                MainThreadTooLongException exception = new MainThreadTooLongException(msg2);
                                exception.setStackTrace(stackTraceElements1);
                                if(MyDokit.getConfig() != null){
                                    MyDokit.getConfig().report(exception);
                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private  void  printApplicationStartTime(String methodName) {
        Log.i(TAG, "================Dokit Application start================");
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.toString().contains("MethodCostUtil")) {
                continue;
            }
            if (stackTraceElement.toString().contains("dalvik.system.VMStack.getThreadStackTrace")) {
                continue;
            }
            if (stackTraceElement.toString().contains("java.lang.Thread.getStackTrace")) {
                continue;
            }
            Log.i(TAG, "\tat "+stackTraceElement);
        }
        Log.i(TAG, "================Dokit Application  end================");
        Log.i(TAG, "\n");
    }

    private  void  printActivityStartTime(String methodName) {
        Log.i(TAG, "================Dokit Activity start================");
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTraceElements) {
            if (stackTraceElement.toString().contains("MethodCostUtil")) {
                continue;
            }
            if (stackTraceElement.toString().contains("dalvik.system.VMStack.getThreadStackTrace")) {
                continue;
            }
            if (stackTraceElement.toString().contains("java.lang.Thread.getStackTrace")) {
                continue;
            }
            Log.i(TAG, "\tat "+stackTraceElement);
        }
        Log.i(TAG, "================Dokit Activity end================");
        Log.i(TAG, "\n");
    }

    /**
     * 静态方法
     *
     * @param thresholdTime 预设的值 单位为us 1000us = 1ms
     * @param methodName
     */
    public  void  recodeStaticMethodCostEnd(int thresholdTime, String  methodName) {
        recodeObjectMethodCostEnd(thresholdTime, methodName, staticMethodObject);
    }
}
