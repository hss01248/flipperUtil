package com.hss01248.dokit;

import com.hss01248.dokit.configs.IUserInfoForDokit;
import com.hss01248.dokit.parts.BaseButton;
import com.hss01248.dokit.parts.BaseSwitcherKit;

/**
 * http://xingyun.xiaojukeji.com/docs/dokit/#/androidGuide
 */
public class MyDokit {


    public static IUserInfoForDokit getUserInfoImplForDokit() {
        return userInfoImplForDokit;
    }

    public static void setUserInfoImplForDokit(IUserInfoForDokit userInfoImplForDokit) {
        MyDokit.userInfoImplForDokit = userInfoImplForDokit;
    }

    static IUserInfoForDokit userInfoImplForDokit;

    public static void setConfig(IDokitConfig iWebDoorl) {
        MyDokit.iWebDoorl = iWebDoorl;
    }

    /**
     * 设置启动顺序: InitForDokit在谁之后启动
     * @param initClassName
     */
    public static void setExtraInitClassName(String initClassName){

    }

    public static void setProductId(String productId) {
        MyDokit.productId = productId;
    }

    private static String productId = "";

    public static IDokitConfig getConfig() {
        return iWebDoorl;
    }

    static IDokitConfig iWebDoorl;







    /**
     * 同时还需要在assets的dokit_system_kits.json里添加id和类路径
     * @param customButton
     */
    public static void addButton(BaseButton customButton) {

    }

    /**
     * 同时还需要在assets的dokit_system_kits.json里添加id和类路径
     * @param switcherKit
     */
    public static void addSwitch(BaseSwitcherKit switcherKit) {

    }


}
