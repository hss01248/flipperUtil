package com.hss01248.dokit.parts;

import android.content.Context;


/**
 * by hss
 * data:2020-04-29
 * desc:
 */
public class BaseButton  {




    public BaseButton(ICustomButton button) {
        this.button = button;
    }

    ICustomButton button;
    
    public int getCategory() {
        return 1;
    }

    
    public int getIcon() {
        if(button.getIcon() != 0){
            return button.getIcon();
        }
        return 0;
    }

    
    public int getName() {
        return button.getName();
    }

    
    public void onAppInit(Context context) {
        button.onAppInit(context);

    }

    
    public void onClick(Context context) {
        button.onClick();
    }
}
