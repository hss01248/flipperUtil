package com.hss01248.dokit.parts;

import android.content.Context;

import com.hss01248.dokit.R;

/**
 * by hss
 * data:2020-04-29
 * desc:
 */
public abstract class ICustomButton {

   public abstract int getName();

    public abstract void onClick();

   public int getIcon(){
       return R.drawable.tools_base_btn_bg_selector;
   }

    public void onAppInit(Context context) {

    }
}
