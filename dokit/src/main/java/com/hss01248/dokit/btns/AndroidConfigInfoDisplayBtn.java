package com.hss01248.dokit.btns;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Configuration;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.dokit.R;
import com.hss01248.dokit.parts.ICustomButton;

import java.util.HashMap;
import java.util.Map;

/**
 * @Despciption todo
 * @Author hss
 * @Date 11/07/2022 14:29
 * @Version 1.0
 */
public class AndroidConfigInfoDisplayBtn extends ICustomButton {
    @Override
    public int getName() {
        return R.string.tools_build_activity_info;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_info_testtool;
    }

    @Override
    public void onClick() {


        String msg = getAndroidConfig();
        AlertDialog dialog = new AlertDialog.Builder(ActivityUtils.getTopActivity())
                .setTitle("android Config信息")
                .setMessage(msg)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        dialog.show();
    }

    private static String getAndroidConfig() {
        Activity activity = ActivityUtils.getTopActivity();
        Configuration activityConfiguration = activity.getResources().getConfiguration();

        Configuration appConfiguration = Utils.getApp().getResources().getConfiguration();

        Map map = new HashMap();
        map.put("activityConfiguration",activityConfiguration);
        map.put("appConfiguration",appConfiguration);
        return GsonUtils.getGson("nice").toJson(map);
    }

}
