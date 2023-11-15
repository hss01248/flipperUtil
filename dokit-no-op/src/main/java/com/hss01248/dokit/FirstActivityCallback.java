package com.hss01248.dokit;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @Despciption todo
 * @Author hss
 * @Date 09/06/2022 10:43
 * @Version 1.0
 */
public abstract class FirstActivityCallback implements Application.ActivityLifecycleCallbacks {

    boolean notFirst = false;


    public abstract void onFirstActivityCreated(Activity activity);
    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        if(!notFirst){
            notFirst = true;
            onFirstActivityCreated(activity);
        }
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
