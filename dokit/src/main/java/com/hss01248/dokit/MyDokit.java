package com.hss01248.dokit;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.didichuxing.doraemonkit.DoraemonKit;
import com.didichuxing.doraemonkit.kit.AbstractKit;
import com.glance.guolindev.Glance;
import com.glance.guolindev.logic.model.DBFile;
import com.glance.guolindev.ui.db.DBActivity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hss01248.dokit.btns.ActivityTaskViewBtn;
import com.hss01248.dokit.btns.DbDebugBtn;
import com.hss01248.dokit.parts.BaseButton;
import com.hss01248.dokit.parts.ICustomButton;
import com.hss01248.dokit.parts.ThirdToolKit;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cc.rome753.activitytask2.ActivityTaskHelper;

/**
 * http://xingyun.xiaojukeji.com/docs/dokit/#/androidGuide
 */
public class MyDokit {

    public static void init(Application context){
        List<AbstractKit> kits = new ArrayList<>();
        kits.add(new ThirdToolKit());
        addKits(kits);
        //kits.add(new DemoKit());
        DoraemonKit.install(context, kits,"a61e6101a5afe938cca16087236b8526");
    }

    private static void addKits(List<AbstractKit> kits) {
        kits.add(new BaseButton(new ICustomButton() {
            @Override
            public int getName() {
                return R.string.testkit_go_setting;
            }

            @Override
            public void onClick() {
                InitForDokit.goAppSettings();
            }
        }));
        kits.add(new BaseButton(new ActivityTaskViewBtn()));
        kits.add(new BaseButton(new DbDebugBtn()));
    }
}
