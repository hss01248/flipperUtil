package com.hss01248.flipperdemo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.facebook.flipper.plugins.network.NetworkReporter;
import com.google.gson.reflect.TypeToken;
import com.hjq.permissions.XXPermissions;
import com.hss01248.dokit.MyDokit;
import com.hss01248.dokit.parts.BaseButton;
import com.hss01248.dokit.parts.ICustomButton;
import com.hss01248.flipper.FlipperUtil;
import com.hss01248.http.HttpUtil;
import com.hss01248.http.INetTool;
import com.hss01248.jenkins.JenkinsTool;
import com.liulishuo.filedownloader.FileDownloader;

import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okio.Buffer;

public class BaseApp extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        //BodyUtil2.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initHttp();
        FlipperUtil.setRequestBodyParser(new com.facebook.flipper.plugins.network.RequestBodyParser() {
            @Override
            public boolean parseRequestBoddy(Request request, Buffer bodyBuffer,
                                             NetworkReporter.RequestInfo info,
                                             Map<String, String> bodyMetaData) {
                return false;
            }
        });

        XXPermissions.setScopedStorage(true);
        addBtns();


    }

    private void addBtns() {
        JenkinsTool.init("xxx",
                "yyy",  "ttt");
        FileDownloader.setupOnApplicationOnCreate(this);
        MyDokit.addButton(new BaseButton(new ICustomButton() {
            @Override
            public int getName() {
                return R.string.btn_jenkins;
            }

            @Override
            public void onClick() {
                JenkinsTool.showBuildList((FragmentActivity) ActivityUtils.getTopActivity());
            }
        }));
    }

    private void initHttp() {
        HttpUtil.init(this, true, "https://www.baidu.com", new INetTool() {
            @Override
            public String toJsonStr(Object obj) {
                return GsonUtils.toJson(obj);
            }

            @Override
            public <T> T parseObject(String str, Class<T> clazz) {
                return GsonUtils.getGson().fromJson(str, clazz);
            }

            @Override
            public <E> List<E> parseArray(String str, Class<E> clazz) {
                return GsonUtils.getGson().fromJson(str, new TypeToken<List<E>>(){}.getType());
            }

            @Override
            public void logi(String str) {
                LogUtils.i(str);
            }

            @Override
            public void logd(String str) {
                LogUtils.d(str);
            }

            @Override
            public void logw(String str) {
                LogUtils.w(str);
            }

            @Override
            public void logdJson(String json) {
                LogUtils.json(json);
            }

            @Override
            public void initialStetho(Application application) {

            }

            @Override
            public void addChuckInterceptor(OkHttpClient.Builder builder) {

            }

            @Override
            public void addStethoInterceptor(OkHttpClient.Builder builder) {

            }

            @Override
            public void addHttpLogInterceptor(OkHttpClient.Builder builder) {

            }

            @Override
            public Activity getTopActivity() {
                return ActivityUtils.getTopActivity();
            }

            @Override
            public void logObj(Object t) {
            LogUtils.d(t);
            }

            @Override
            public void reportError(String code, String msg, String url) {

            }
        });
    }




}
