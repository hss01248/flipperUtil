package com.hss01248.flipperdemo;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.facebook.flipper.plugins.network.NetworkReporter;

import com.hjq.permissions.XXPermissions;
import com.hss01248.dokit.IDokitConfig;
import com.hss01248.dokit.MyDokit;
import com.hss01248.dokit.parts.BaseSwitcherKit;
import com.hss01248.dokit.parts.ISwitch;
import com.hss01248.flipper.FlipperUtil;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

import okhttp3.Request;
import okio.Buffer;

public class BaseApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlipperUtil.setRequestBodyParser(new com.facebook.flipper.plugins.network.RequestBodyParser() {
            @Override
            public boolean parseRequestBoddy(Request request, Buffer bodyBuffer, NetworkReporter.RequestInfo info, Map<String, String> bodyMetaData) {
                return false;
            }
        });

        XXPermissions.setScopedStorage(true);

        MyDokit.setConfig(new IDokitConfig() {
            @Override
            public void loadUrl(Context context, String url) {
                ToastUtils.showLong("使用webview加载:"+url);
            }

            @Override
            public void report(Object o) {
                LogUtils.w(o);
            }
        });
        MyDokit.addSwitch(new BaseSwitcherKit(new ISwitch() {
            @Override
            public int key() {
                return R.string.testkit_go_setting;
            }

            @Override
            public boolean originalState() {
                return false;
            }

            @Override
            public void stateWhenInit(boolean state) {

            }

            @Override
            public boolean onIconClick(Runnable changeState, boolean currentState) {
                return false;
            }
        }));

    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return super.getSharedPreferences(name, mode);
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return super.openFileInput(name);
    }

    @Override
    public Resources getResources() {
       // super.getResources().getConfiguration().
        return super.getResources();
    }

    @Override
    public ContentResolver getContentResolver() {
        return super.getContentResolver();
    }

    @Override
    public File getCacheDir() {
        return super.getCacheDir();
    }

    @Override
    public File getFilesDir() {
        return super.getFilesDir();
    }



}
