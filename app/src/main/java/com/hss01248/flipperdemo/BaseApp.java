package com.hss01248.flipperdemo;

import android.app.Application;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.facebook.flipper.plugins.network.NetworkReporter;

import com.hjq.permissions.XXPermissions;
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
