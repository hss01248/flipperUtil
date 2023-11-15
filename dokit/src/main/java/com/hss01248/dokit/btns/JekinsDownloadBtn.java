package com.hss01248.dokit.btns;

import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.ActivityUtils;
import com.hss01248.dokit.R;
import com.hss01248.dokit.parts.ICustomButton;
import com.hss01248.jenkins.JenkinsTool;

/**
 * @Despciption todo
 * @Author hss
 * @Date 15/11/2023 16:30
 * @Version 1.0
 */
public class JekinsDownloadBtn extends ICustomButton {
    @Override
    public int getName() {
        return R.string.tools_jekins;
    }

    @Override
    public void onClick() {
        JenkinsTool.showBuildList((FragmentActivity) ActivityUtils.getTopActivity());
    }
}
