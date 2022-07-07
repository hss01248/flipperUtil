package com.readystatesoftware.chuck;

/**
 * time:2019/11/23
 * author:hss
 * desription:
 */
public interface IChuckPwCheck {

    void check(String input,Runnable success,Runnable fail);

}
