package com.hss01248.dokit.btns;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;


import com.didichuxing.doraemonkit.kit.AbstractKit;
import com.didichuxing.doraemonkit.kit.Category;
import com.didichuxing.doraemonkit.util.SPStaticUtils;
import com.didichuxing.doraemonkit.util.ToastUtils;
import com.hss01248.dokit.R;



import me.ele.uetool.UETool;

public class UEKit extends AbstractKit {

   public static boolean show;
    Application.ActivityLifecycleCallbacks callbacks;
    @Override
    public int getCategory() {
        return Category.TOOLS;
    }

    @Override
    public int getName() {
        if(show){
            return R.string.testkit_UE;
        }else {
            return R.string.testkit_UE_off;
        }
    }
    @Override
    public String innerKitId() {
        return "dokit_sdk_ui_uettol";
    }

    @Override
    public boolean isInnerKit() {
        return true;
    }

    @Override
    public int getIcon() {
        return me.ele.uetool.R.drawable.uet_menu;
    }

    @Override
    public void onClick(Context context) {
        show = !show;
        SPStaticUtils.put("UEKit", show);
        String text = show ? "开启" : "关闭";
        ToastUtils.showShort("UETool已"+text);
        if(show){
            UETool.showUETMenu(uetoolDismissY);
        }else {
            uetoolDismissY =  UETool.dismissUETMenu();
        }
    }
    private int uetoolDismissY = 200;
    @Override
    public void onAppInit(Context context) {
        show = SPStaticUtils.getBoolean("UEKit", false);
        callbacks = new Application.ActivityLifecycleCallbacks() {
            private int visibleActivityCount;

            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                visibleActivityCount++;
                if (visibleActivityCount >= 1) {
                    if(show) {
                        UETool.showUETMenu(uetoolDismissY);
                    }
                }

            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

                visibleActivityCount--;
                if (visibleActivityCount == 0) {
                    if(show){
                        uetoolDismissY = UETool.dismissUETMenu();
                    }
                }

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        };
        ((Application)context).registerActivityLifecycleCallbacks(callbacks);
    }
}
