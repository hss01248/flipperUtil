package com.hss01248.dokit.parts;

import android.content.Context;

import com.didichuxing.doraemonkit.kit.AbstractKit;
import com.didichuxing.doraemonkit.kit.Category;
import com.hss01248.dokit.R;


/**
 * by hss
 * data:2020-04-29
 * desc:
 */
public class BaseButton  extends AbstractKit {




    public BaseButton(ICustomButton button) {
        this.button = button;
    }

    ICustomButton button;
    @Override
    public int getCategory() {
        return Category.TOOLS;
    }

    @Override
    public int getIcon() {
        if(button.getIcon() != 0){
            return button.getIcon();
        }
        return R.drawable.dk_add_shape;
    }

    @Override
    public int getName() {
        return button.getName();
    }

    @Override
    public void onAppInit(Context context) {
        button.onAppInit(context);

    }

    @Override
    public void onClick(Context context) {
        button.onClick();
    }
}
