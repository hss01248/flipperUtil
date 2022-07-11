package com.hss01248.dokit.btns;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.dokit.R;
import com.hss01248.dokit.parts.ICustomButton;

import java.lang.reflect.Field;

/**
 * @Despciption todo
 * @Author hss
 * @Date 11/07/2022 14:28
 * @Version 1.0
 */
public class BuildTypeInfoDisplayBtn extends ICustomButton {
    @Override
    public int getName() {
        return R.string.tools_buildtype_info;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_info_testtool;
    }

    @Override
    public void onClick() {

        Class buildConfig = buildConfig();
        String msg = "BuildConfig信息未能通过反射得到";
        if(buildConfig != null){
            Field[] declaredFields = buildConfig.getDeclaredFields();
            if(declaredFields != null){
                msg = buildConfig.getName()+":\n\n";
                for (Field field : declaredFields) {
                    field.setAccessible(true);
                    try {
                        msg += (field.getName().toLowerCase()+": "+field.get(buildConfig)+"\n");
                    }catch (Throwable throwable){
                        LogUtils.w(throwable);
                    }
                }
            }
        }
        AlertDialog dialog = new AlertDialog.Builder(ActivityUtils.getTopActivity())
                .setTitle("BuildConfig信息")
                .setMessage(msg)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        dialog.show();
    }

    private static Class buildConfig() {
        try {
            String name0 = Utils.getApp().getPackageName() + ".BuildConfig";
            Class clazz = Class.forName(name0);
            return clazz;
        } catch (Throwable throwable) {
            LogUtils.d("getBuildType", "xx", throwable);
            String name = Utils.getApp().getClass().getName();
            name = name.substring(0, name.lastIndexOf("."));
            while (name.contains(".")){
                String config = name + ".BuildConfig";
                try {
                    Class clazz = Class.forName(config);
                    if(clazz != null){
                        return clazz;
                    }
                } catch (ClassNotFoundException e) {
                    LogUtils.i(e);
                }
                name = name.substring(0, name.lastIndexOf("."));
            }
            return null;
        }
    }
}
