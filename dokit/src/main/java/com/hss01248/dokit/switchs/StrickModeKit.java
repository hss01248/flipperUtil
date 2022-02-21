package com.hss01248.dokit.switchs;

import android.content.Context;
import android.os.StrictMode;

import com.didichuxing.doraemonkit.kit.AbstractKit;
import com.didichuxing.doraemonkit.kit.Category;
import com.didichuxing.doraemonkit.util.SPStaticUtils;
import com.didichuxing.doraemonkit.util.ToastUtils;
import com.hss01248.dokit.R;


public class StrickModeKit extends AbstractKit {
    boolean show;
    @Override
    public int getCategory() {
        return Category.TOOLS;
    }

    @Override
    public int getName() {
        if(show){
            return R.string.testkit_stricMode;
        }else {
            return R.string.testkit_stricMode_off;
        }
    }

    @Override
    public String innerKitId() {
        return "dokit_sdk_perfom_StrickMode";
    }

    @Override
    public boolean isInnerKit() {
        return true;
    }

    @Override
    public int getIcon() {
        return R.drawable.block;
    }

    @Override
    public void onClick(Context context) {
        show = !show;
        SPStaticUtils.put("StrickModeKit", show);
        String text = show ? "开启" : "关闭";
        openStricMode(show);
        ToastUtils.showShort("StrickMode已"+text);
    }

    @Override
    public void onAppInit(Context context) {
        show = SPStaticUtils.getBoolean("StrickModeKit", false);
        if(show){
            openStricMode(show);
        }

    }

    private void openStricMode(boolean open) {
        StrictMode.ThreadPolicy.Builder builder = new StrictMode.ThreadPolicy.Builder();
        if(open){
            builder.detectAll();
        }else {
            builder.permitAll();
        }
        StrictMode.setThreadPolicy(
               builder
                //.penaltyDialog()//弹出违规提示框
                .penaltyLog()//在Logcat中打印违规日志
                .build());
        StrictMode.VmPolicy.Builder builder1 = new StrictMode.VmPolicy.Builder();
        if(open){
            builder.detectAll();
        }else {
            builder.permitAll();
        }
        StrictMode.setVmPolicy(
                builder1
                .penaltyLog()
                .build());
    }
}
