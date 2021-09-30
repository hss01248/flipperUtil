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
public class RNAspect {

    private static final String TAG = "RNAspect";


    @Before("execution(* com.facebook.react.bridge.Promise.*(..))  ||  @annotation(com.facebook.react.bridge.ReactMethod) || execution(* com.facebook.react.bridge.Callback.*(..))")
    public void weaveJoinPoint(JoinPoint joinPoint) throws Throwable {
        if(joinPoint.getThis().getClass().getName().equals("com.facebook.react.uimanager.UIManagerModule")){
            return;
        }
        LogMethodAspect.logBefore(true,TAG,joinPoint,new LogMethodAspect.IBefore(){
            @Override
            public String descExtraForLog(){
                return "";
            }

            @Override
            public void before(JoinPoint joinPoin, String desc) {
                //给rn原生的log打一下
                Log.d("ReactNativeJS-an",desc);
            }
        });
    }


}
