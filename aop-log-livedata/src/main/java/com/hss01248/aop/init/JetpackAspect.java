package com.hss01248.aop.init;




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
public class JetpackAspect {

    private static final String TAG = "jetAspect";


    @Before("execution(* androidx.lifecycle.LiveData.observe(..))  " +
            "||  execution(* androidx.lifecycle.LiveData.postValue(..)) " +
            "|| execution(* androidx.lifecycle.LiveData.setValue(..))  || execution(* androidx.lifecycle.ViewModelProvider.get(java.lang.String, java.lang.Class))")
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
