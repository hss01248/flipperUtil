package com.hss01248.dokit.btns;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.didichuxing.doraemonkit.util.ActivityUtils;
import com.hss01248.dokit.R;
import com.hss01248.dokit.parts.ICustomButton;

import cc.rome753.activitytask2.ActivityTaskHelper;

public class ActivityTaskViewBtn extends ICustomButton {
    @Override
    public int getName() {
        return R.string.testkit_activity_taskview;
    }

    @Override
    public void onAppInit(Context context) {
        super.onAppInit(context);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                ActivityTaskHelper.init((Application) context,null);
            }
        });

    }

    @Override
    public void onClick() {
        ActivityTaskHelper.openOrClose(ActivityUtils.getTopActivity());
        //InitForDokit.context.getSharedPreferences("activitytask",Context.MODE_PRIVATE).edit().putBoolean("open",true);

    }


}
