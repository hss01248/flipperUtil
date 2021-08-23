package com.hss01248.dokit;

import android.app.Application;

import com.didichuxing.doraemonkit.DoraemonKit;
import com.didichuxing.doraemonkit.kit.AbstractKit;
import java.util.ArrayList;
import java.util.List;

/**
 * http://xingyun.xiaojukeji.com/docs/dokit/#/androidGuide
 */
public class MyDokit {

    public static void init(Application context){
        List<AbstractKit> kits = new ArrayList<>();
        //kits.add(new DemoKit());
        DoraemonKit.install(context, kits,"a61e6101a5afe938cca16087236b8526");
    }
}
