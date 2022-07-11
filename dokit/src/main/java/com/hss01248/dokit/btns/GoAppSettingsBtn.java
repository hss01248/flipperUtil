package com.hss01248.dokit.btns;

import com.hss01248.dokit.InitForDokit;
import com.hss01248.dokit.R;
import com.hss01248.dokit.parts.ICustomButton;

/**
 * @Despciption todo
 * @Author hss
 * @Date 11/07/2022 14:47
 * @Version 1.0
 */
public class GoAppSettingsBtn extends ICustomButton {
    @Override
    public int getName() {
        return R.string.testkit_go_setting;
    }

    @Override
    public int getIcon() {
        return R.drawable.tools_go_settings;
    }

    @Override
    public void onClick() {
        InitForDokit.goAppSettings();
    }
}
