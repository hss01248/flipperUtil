package com.hss01248.flipper.eventbus;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.core.FlipperConnection;
import com.facebook.flipper.core.FlipperObject;
import com.facebook.flipper.core.FlipperPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Despciption todo
 * @Author hss
 * @Date 23/12/2021 15:39
 * @Version 1.0
 */
public class EventBusLogger2FlipperPlugin implements FlipperPlugin {
    @Override
    public String getId() {
        return "eventbus-logger";
    }
    static FlipperConnection connection;
    @Override
    public void onConnect(FlipperConnection connection) throws Exception {
        EventBusLogger2FlipperPlugin.connection = connection;
    }

    @Override
    public void onDisconnect() throws Exception {

    }

    @Override
    public boolean runInBackground() {
        return false;
    }
    static SimpleDateFormat dateTimeFormatter = new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",Locale.getDefault());
    static AtomicInteger id0 = new AtomicInteger(0);
    static void sendData(Object event,String eventType){
        if(connection == null){
            LogUtils.w("connection == null");
            return;
        }
        if(AndroidFlipperClient.getInstanceIfInitialized() == null ){
            LogUtils.w("AndroidFlipperClient.getInstanceIfInitialized() == null");
            return;
        }
        StackTraceElement[] stackTrace1 = Thread.currentThread().getStackTrace();
        if("post".equals(eventType)){
            for (StackTraceElement element : stackTrace1) {
               if("postSticky".equals( element.getMethodName())){
                   eventType = "postSticky";
                   break;
               }
            }
        }

        String stackTrace = getStackTraceString(stackTrace1,5);
        String timeStamp = dateTimeFormatter.format(new Date());


        String className = event.getClass().getSimpleName();
        String evenBody = null;
        try {
            evenBody = GsonUtils.toJson(event);
        }catch (Throwable throwable){
            //LogUtils.w(throwable);
            evenBody = event.toString();
        }
        long id  = id0.incrementAndGet();
        FlipperObject.Builder keyValueMap = new FlipperObject.Builder()
                .put("id", id)
                .put("name", className)
                .put("eventType", eventType)
                .put("timestamp", timeStamp);


        FlipperObject.Builder builder = new FlipperObject.Builder();
        FlipperObject obj = builder.put("id", id)
                .put("name", className)
                .put("eventType", eventType)
                .put("eventBody", evenBody)
                .put("timestamp", timeStamp)
                .put("stackTrace", stackTrace)
                .put("keyValueMap", keyValueMap)
                .build();
        connection.send("newEvent",obj);

    }

    static String getStackTraceString( StackTraceElement[] stackTraceElements, int skipPreElements) {
        if (stackTraceElements == null || stackTraceElements.length <= skipPreElements) {
            return null;
        }
        StringBuilder stackTrace = new StringBuilder();
        for (int i = skipPreElements; i < stackTraceElements.length; i++) {
            stackTrace
                    .append(stackTraceElements[i].getClassName())
                    .append(".")
                    .append(stackTraceElements[i].getMethodName())
                    .append("() at Line ")
                    .append(stackTraceElements[i].getLineNumber())
                    .append("\n\n");
        }
        return stackTrace.toString();
    }
}
