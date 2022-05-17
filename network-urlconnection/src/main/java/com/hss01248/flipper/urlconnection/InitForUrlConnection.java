package com.hss01248.flipper.urlconnection;

import android.content.Context;


import androidx.startup.Initializer;

import com.facebook.flipper.plugins.network.ProxyUrlConnectionUtil;

import java.util.ArrayList;
import java.util.List;

public class InitForUrlConnection implements Initializer<String> {
   public static Context context;
    @Override
    public String create(Context context) {
        ProxyUrlConnectionUtil.proxyUrlConnection();
        return "InitForUrlConnection";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {

        return new ArrayList<>();
    }


}
