package com.hss01248.dokit;

import android.app.Application;
import android.content.Context;

import com.didichuxing.doraemonkit.DoraemonKit;
import com.didichuxing.doraemonkit.kit.AbstractKit;
import com.didichuxing.doraemonkit.kit.webdoor.WebDoorManager;
import com.hss01248.dokit.btns.ActivityTaskViewBtn;
import com.hss01248.dokit.btns.DbDebugBtn;
import com.hss01248.dokit.parts.BaseButton;
import com.hss01248.dokit.parts.ICustomButton;
import com.hss01248.dokit.parts.ThirdToolKit;

import java.util.ArrayList;
import java.util.List;

/**
 * http://xingyun.xiaojukeji.com/docs/dokit/#/androidGuide
 */
public class MyDokit {

    public static void setConfig(IDokitConfig iWebDoorl) {
        MyDokit.iWebDoorl = iWebDoorl;
    }

    public static IDokitConfig getConfig() {
        return iWebDoorl;
    }

    static IDokitConfig iWebDoorl;

     static void init(Application context){
        List<AbstractKit> kits = new ArrayList<>();
        kits.add(new ThirdToolKit());
        addKits(kits);
        //kits.add(new DemoKit());
        DoraemonKit.setWebDoorCallback(new WebDoorManager.WebDoorCallback() {
            @Override
            public void overrideUrlLoading(Context context, String url) {
                if(iWebDoorl != null){
                    iWebDoorl.loadUrl(context, url);
                }

            }
        });
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
