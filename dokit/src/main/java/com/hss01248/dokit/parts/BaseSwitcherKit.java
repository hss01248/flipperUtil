package com.hss01248.dokit.parts;

import android.content.Context;
import android.widget.Toast;

import com.didichuxing.doraemonkit.kit.AbstractKit;
import com.didichuxing.doraemonkit.kit.Category;
import com.didichuxing.doraemonkit.util.SPUtils;
import com.hss01248.dokit.R;


/**
 * by hss
 * data:2020-04-24
 * desc:
 * @author hss
 */
public class BaseSwitcherKit extends AbstractKit {
    boolean last;

    public BaseSwitcherKit(ISwitch iSwitch) {
        this.iSwitch = iSwitch;
    }

    ISwitch iSwitch;
    @Override
    public int getCategory() {
        return Category.TOOLS;
    }

    @Override
    public int getName() {
        return iSwitch.key();
    }

    @Override
    public int getIcon() {
        if(last){
            return R.drawable.tools_switch_on;
        }
        return R.drawable.tools_switch_off;
    }

    @Override
    public void onClick(final Context context) {
        final boolean currentState = SPUtils.getInstance().getBoolean(context.getString(getName()),iSwitch.originalState());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                SPUtils.getInstance().put(context.getString(getName()),!currentState);
                last = !currentState;
                Toast.makeText(context,context.getString(getName())+ " 状态已变更为:"+!currentState,Toast.LENGTH_SHORT).show();
            }
        };
        if(!iSwitch.onIconClick(runnable,currentState)){
            runnable.run();
        }

    }

    @Override
    public void onAppInit(Context context) {
         last = SPUtils.getInstance().getBoolean(context.getString(getName()),iSwitch.originalState());
        iSwitch.stateWhenInit(last);
    }
}
