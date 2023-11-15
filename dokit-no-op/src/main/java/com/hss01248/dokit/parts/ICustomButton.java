package com.hss01248.dokit.parts;

import android.content.Context;

/**
 * by hss
 * data:2020-04-29
 * desc:
 */
public abstract class ICustomButton {

   public abstract int getName();

    public abstract void onClick();

   public int getIcon(){
       return 0;
   }

    public void onAppInit(Context context) {

    }
}
