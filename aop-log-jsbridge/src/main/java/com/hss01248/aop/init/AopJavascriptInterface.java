package com.hss01248.aop.init;

import com.flyjingfish.android_aop_annotation.anno.AndroidAopPointCut;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 与 {@link android.webkit.JavascriptInterface} 同时标在桥接方法上，启用 Js 调试日志（AndroidAOP）。
 */
@AndroidAopPointCut(JsObjCut.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AopJavascriptInterface {
}
