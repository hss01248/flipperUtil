package com.hss01248.dokit.parts;

/**
 * by hss
 * data:2020-04-24
 * desc: 开关
 */
public interface ISwitch {

    /**
     * String资源
     * @return
     */
    int key();

    boolean originalState();

    void stateWhenInit(boolean state);

    /**
     *
     * @param changeState
     * @return 如果返回true,代表拦截,不再自动调用更改sp状态的操作
     */
    boolean onIconClick(Runnable changeState,boolean currentState);

}
