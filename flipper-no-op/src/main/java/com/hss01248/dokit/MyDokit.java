package com.hss01248.dokit;



/**
 * http://xingyun.xiaojukeji.com/docs/dokit/#/androidGuide
 */
public class MyDokit {

    public static void setConfig(IDokitConfig iWebDoorl) {
        MyDokit.iWebDoorl = iWebDoorl;
    }

    public static IDokitConfig getConfig() {
        return iWebDoorl;
    }

    static IDokitConfig iWebDoorl;


}
