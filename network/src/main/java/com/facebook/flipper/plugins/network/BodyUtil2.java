package com.facebook.flipper.plugins.network;

import android.app.Application;
import android.content.Context;

import java.net.URL;

import me.weishu.reflection.Reflection;

/**
 * @author: Administrator
 * @date: 2022/2/12
 * @desc: //todo
 */
public class BodyUtil2 {

    public static void attachBaseContext(Context base) {
        try {
            Reflection.unseal(base);
        }catch (Throwable throwable){
            throwable.printStackTrace();
        }


    }




}
