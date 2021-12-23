package com.hss01248.flipper.eventbus;

import com.blankj.utilcode.util.LogUtils;

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


    @Before("execution(* org.greenrobot.eventbus.EventBus.post(..))  " +
            "|| execution(* org.greenrobot.eventbus.EventBus.removeStickyEvent(..))")
    //||  execution(* org.greenrobot.eventbus.EventBus.postSticky(..))
    public void weaveJoinPoint(JoinPoint joinPoint) throws Throwable {
        LogUtils.d("bus",joinPoint);
        EventBusLogger2FlipperPlugin.sendData(joinPoint.getArgs()[0],joinPoint.getSignature().getName());
    }


}
