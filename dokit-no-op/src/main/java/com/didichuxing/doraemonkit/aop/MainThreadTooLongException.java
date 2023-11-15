package com.didichuxing.doraemonkit.aop;

import android.os.Build;

import androidx.annotation.RequiresApi;

public class MainThreadTooLongException extends Exception{

    public MainThreadTooLongException() {
    }

    public MainThreadTooLongException(String message) {
        super(message);
    }

    public MainThreadTooLongException(String message, Throwable cause) {
        super(message, cause);
    }

    public MainThreadTooLongException(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public MainThreadTooLongException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
