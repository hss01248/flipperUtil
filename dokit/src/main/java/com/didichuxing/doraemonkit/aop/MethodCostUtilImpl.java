package com.didichuxing.doraemonkit.aop;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.util.Log;


import com.blankj.utilcode.util.ThreadUtils;
import com.didichuxing.doraemonkit.aop.method_stack.StaticMethodObject;
import com.didichuxing.doraemonkit.kit.timecounter.TimeCounterManager;
import com.hss01248.dokit.MyDokit;


import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class MethodCostUtilImpl {

    private static String TAG = "DOKIT_SLOW_METHOD";
    public static int MIN_TIME = 167;//MS
    private static ConcurrentHashMap<String, Long> METHOD_COSTS = new ConcurrentHashMap<>();

    private static StaticMethodObject staticMethodObject = new  StaticMethodObject();


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
                            if(!methodName.contains("Interceptor&intercept")){
                                //大概率是okhttp的拦截器,不打印
                                Log.d(TAG, "================not main method, do not report================> "+ methodName+" cost(ms): "+ costTime+""+" thread:"+threadName);
                            }
                            return;
                        }
                        Log.i(TAG, "================Dokit================");
                        //String msg = "methodName===>"+methodName+"  threadName==>"+threadName+"  thresholdTime===>"+thresholdTime+"   costTime===>"+costTime;
                        methodName = methodName.replace("&",".");
                        methodName = methodName+"()";
                        //com.akulaku.module.product.databinding.ProductActivitySearchResultV2Binding.inflate()
                        String[] descs = methodName.replace("()","").split("\\.");
                        String desc = descs[descs.length-2]+"_"+descs[descs.length-1];
                        String msg2 = methodName+"   cost(ms)====>"+costTime;
                        Log.i(TAG, "\t "+msg2);
                        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
                        //上报到bugly

                        //recodeStaticMethodCostEnd
                        StackTraceElement[] stackTraceElements1 = new StackTraceElement[stackTraceElements.length-6];
                        for (int i = 0; i < stackTraceElements.length-6; i++) {
                            stackTraceElements1[i] = stackTraceElements[i+6];
                            //Log.i(TAG, "\tat "+stackTraceElements1[i]);
                        }
                        if("recodeStaticMethodCostEnd".equals(stackTraceElements1[0].getMethodName())){
                            Log.w(TAG, "冗余的栈: +recodeStaticMethodCostEnd");
                            return;
                        }
                        //todo 判断方法栈的包含关系
                        if(contains0(preStacks,stackTraceElements1)){
                            Log.d(TAG, "重复的栈,不上报,归类为第一个原始栈 :\t"+stackTraceElements1[0]);
                            /*for (StackTraceElement element : stackTraceElements1) {
                                Log.d(TAG, "\tat "+element);
                            }*/
                            return;
                        }else {
                            preStacks.add(stackTraceElements1);
                            ThreadUtils.getMainHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    preStacks.remove(stackTraceElements1);
                                }
                            },1000);
                            Log.w(TAG, "新的栈,上报 ");
                            for (StackTraceElement element : stackTraceElements1) {
                                Log.i(TAG, "\tat "+element);
                            }
                        }
                        if(MyDokit.getConfig() != null){
                            ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Object>() {
                                @Override
                                public Object doInBackground() throws Throwable {
                                    MainThreadTooLongException exception = new MainThreadTooLongException(msg2);
                                    exception.setStackTrace(stackTraceElements1);
                                    String methodDesc = desc+"_"+stackTraceElements1[0].getLineNumber();
                                    MyDokit.getConfig().onMainBlock(costTime,MIN_TIME,methodDesc,exception);

                               /* TraceInfo.create("method_block_main_thread_167")
                                        .setMainMetric(costTime)
                                        //.addMetirc("blockTime_ms",costTime)
                                        .addAttribute("methodDesc",desc+"_"+stackTraceElements1[0].getLineNumber())
                                        .report();*/
                                    return null;
                                }

                                @Override
                                public void onSuccess(Object result) {

                                }
                            });
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean contains0(List<StackTraceElement[]> preStackList, StackTraceElement[] stackTraceElements1){
        try {
            for (StackTraceElement[] pre : preStackList) {
                boolean contains = stackContains(pre,stackTraceElements1);
                if(contains){
                    return true;
                }
            }
            return false;
        }catch (Throwable throwable){
            Log.w(TAG,throwable);
            return false;
        }

    }

    private boolean stackContains(StackTraceElement[] preStacks, StackTraceElement[] stackTraceElements1) {
        int length = stackTraceElements1.length;
        if(preStacks.length < stackTraceElements1.length){
            return false;
        }
        int lengthPre = preStacks.length -1;
        for (int i = length-1; i >= 0; i--) {
            StackTraceElement element =  stackTraceElements1[i];
            StackTraceElement preEle = preStacks[lengthPre];
            if(element.getClassName().equals(preEle.getClassName())
            && element.getMethodName().equals(preEle.getMethodName())
            && Math.abs(element.getLineNumber() - preEle.getLineNumber())< 10){
                //&& element.getFileName().equals(preEle.getFileName()) //会空指针
                //todo 因为asm的原因,行号可能会相差一点点
                //BaseVMFragment.onViewCreated(BaseVMFragment.java:57)
                //BaseVMFragment.onViewCreated(BaseVMFragment.java:58)
                lengthPre --;
                continue;
            }
            return false;
        }
        return true;
    }

    static List<StackTraceElement[]> preStacks = new CopyOnWriteArrayList<>();

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
