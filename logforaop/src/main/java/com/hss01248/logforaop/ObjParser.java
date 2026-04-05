package com.hss01248.logforaop;

/**
 * 轻量对象描述（替代原 logforaop 库中的 ObjParser，供调试日志使用）。
 */
public final class ObjParser {

    private ObjParser() {
    }

    public static String parseObj(Object o) {
        if (o == null) {
            return "null";
        }
        try {
            return o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
        } catch (Throwable t) {
            return String.valueOf(o);
        }
    }
}
