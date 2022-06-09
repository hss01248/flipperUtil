package com.hss01248.dokit;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.didichuxing.doraemonkit.DoraemonKit;
import com.didichuxing.doraemonkit.kit.AbstractKit;
import com.didichuxing.doraemonkit.kit.webdoor.WebDoorManager;
import com.hss01248.dokit.btns.ActivityTaskViewBtn;
import com.hss01248.dokit.btns.DbDebugBtn;
import com.hss01248.dokit.btns.UEKit;
import com.hss01248.dokit.parts.BaseButton;
import com.hss01248.dokit.parts.BaseSwitcherKit;
import com.hss01248.dokit.parts.ICustomButton;
import com.hss01248.dokit.parts.ThirdToolKit;
import com.hss01248.dokit.switchs.StrickModeKit;

import java.util.ArrayList;
import java.util.List;

/**
 * http://xingyun.xiaojukeji.com/docs/dokit/#/androidGuide
 */
public class MyDokit {

    public static void setConfig(IDokitConfig iWebDoorl) {
        MyDokit.iWebDoorl = iWebDoorl;
    }

    /**
     * 设置启动顺序: InitForDokit在谁之后启动
     * @param initClassName
     */
    public static void setExtraInitClassName(String initClassName){
        InitForDokit.setExtraInitClassName(initClassName);
    }

    public static void setProductId(String productId) {
        MyDokit.productId = productId;
    }

    private static String productId = "a61e6101a5afe938cca16087236b8526";

    public static IDokitConfig getConfig() {
        return iWebDoorl;
    }

    static IDokitConfig iWebDoorl;

    static  List<AbstractKit> extraKits = new ArrayList<>();

     static void init(Application context){
        List<AbstractKit> kits = new ArrayList<>();
        kits.add(new ThirdToolKit());
         addKitsInternal(kits);

        //kits.add(new DemoKit());
        DoraemonKit.setWebDoorCallback(new WebDoorManager.WebDoorCallback() {
            @Override
            public void overrideUrlLoading(Context context, String url) {
                if(iWebDoorl != null){
                    iWebDoorl.loadUrl(context, url);
                }

            }
        });

        context.registerActivityLifecycleCallbacks(new FirstActivityCallback() {
            @Override
            public void onFirstActivityCreated(Activity activity) {
                if(!extraKits.isEmpty()){
                    kits.addAll(extraKits);
                }
                DoraemonKit.install(context, kits,productId);
            }
        });
    }

    private static void addKitsInternal(List<AbstractKit> kits) {
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
        kits.add(new StrickModeKit());
        kits.add(new UEKit());

    }

    /**
     * 同时还需要在assets的dokit_system_kits.json里添加id和类路径
     * @param customButton
     */
    public static void addButton(BaseButton customButton) {
        extraKits.add(customButton);
    }

    /**
     * 同时还需要在assets的dokit_system_kits.json里添加id和类路径
     * @param switcherKit
     */
    public static void addSwitch(BaseSwitcherKit switcherKit) {
        extraKits.add(switcherKit);
    }


}
