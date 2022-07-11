package com.hss01248.dokit.btns;

import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ActivityUtils;
import com.hss01248.dokit.MyDokit;
import com.hss01248.dokit.R;
import com.hss01248.dokit.parts.ICustomButton;

/**
 * @Despciption todo
 * @Author hss
 * @Date 11/07/2022 14:23
 * @Version 1.0
 */
public class UserInfoDisplayBtn extends ICustomButton {
    @Override
    public int getName() {
        return R.string.tools_user_info;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_info_testtool;
    }

    @Override
    public void onClick() {
        String msg = "MyDokit.getConfig()未设置";

        if( MyDokit.getConfig() != null){
            msg = MyDokit.getConfig().getUserInfo();
        }
        AlertDialog dialog = new AlertDialog.Builder(ActivityUtils.getTopActivity())
                .setTitle("用户信息")
                .setMessage(msg)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        dialog.show();
    }
}
