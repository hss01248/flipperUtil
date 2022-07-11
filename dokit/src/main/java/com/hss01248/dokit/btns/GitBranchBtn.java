package com.hss01248.dokit.btns;

import com.blankj.utilcode.util.ReflectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.hss01248.dokit.R;
import com.hss01248.dokit.parts.ICustomButton;

/**
 * @Despciption todo
 * @Author hss
 * @Date 11/07/2022 14:21
 * @Version 1.0
 */
public class GitBranchBtn extends ICustomButton {
    @Override
    public int getName() {
        return R.string.tools_show_branch;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_tool_git;
    }

    @Override
    public void onClick() {
        try {
            String str = ReflectUtils.reflect(Utils.getApp().getPackageName()+".BuildConfig").field("BRANCH").get();
            ToastUtils.showLong(str);
        }catch (Throwable throwable){
            throwable.printStackTrace();
            ToastUtils.showLong("请在buildtype里配置: \n buildConfigField 'String', 'BRANCH', '\"' + getGitBranch() + '\"'");
        }

    }
}
