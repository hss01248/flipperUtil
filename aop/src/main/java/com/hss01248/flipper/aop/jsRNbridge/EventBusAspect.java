package com.hss01248.flipper.aop.jsRNbridge;




import android.util.Log;

import com.hss01248.logforaop.LogMethodAspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;


/**
 * by hss
 * data:2020/7/17
 * desc:
 */
@Aspect
public class EventBusAspect {

    private static final String TAG = "busAspect";


    @Before("execution(* org.greenrobot.eventbus.EventBus.post(..))  ||  execution(* org.greenrobot.eventbus.EventBus.postSticky(..)) || @annotation(org.greenrobot.eventbus.Subscribe)")
    public void weaveJoinPoint(JoinPoint joinPoint) throws Throwable {
        LogMethodAspect.logBefore(true,TAG,joinPoint,new LogMethodAspect.IBefore(){
            @Override
            public String descExtraForLog(){
                return "";
            }

            @Override
            public void before(JoinPoint joinPoin, String desc) {
                //给rn原生的log打一下
                //Log.d("ReactNativeJS-an",desc);
            }
        });
    }


}
