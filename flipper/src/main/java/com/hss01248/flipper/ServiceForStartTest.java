package com.hss01248.flipper;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

/**
 * @Despciption todo
 * @Author hss
 * @Date 29/06/2022 14:18
 * @Version 1.0
 */
public class ServiceForStartTest extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
