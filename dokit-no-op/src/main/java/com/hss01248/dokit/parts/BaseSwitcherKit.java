package com.hss01248.dokit.parts;

import android.content.Context;


/**
 * by hss
 * data:2020-04-24
 * desc:
 * @author hss
 */
public class BaseSwitcherKit  {
    boolean last;

    public BaseSwitcherKit(ISwitch iSwitch) {
        this.iSwitch = iSwitch;
    }

    ISwitch iSwitch;
    
    public int getCategory() {
        return 0;
    }

    
    public int getName() {
        return iSwitch.key();
    }

    
    public int getIcon() {

        return 0;
    }

    
    public void onClick(final Context context) {


    }

    
    public void onAppInit(Context context) {

    }
}
