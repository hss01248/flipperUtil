package com.hss01248.aop.utilcode;

import androidx.annotation.Nullable;

public interface ILogReport {

    void report(String logLevel, @Nullable String tag, String msg, @Nullable Throwable throwable, Object... args);
}
