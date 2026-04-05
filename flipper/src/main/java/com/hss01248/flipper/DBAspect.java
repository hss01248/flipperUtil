package com.hss01248.flipper;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Flipper 外部数据库路径织入（AndroidAOP）。
 */
public class DBAspect {

    private static final String TAG = "DBAspect";
    static List<String> outDbFiles = new ArrayList<>();
    static boolean parsed = false;

    public static void addDB(File dbFile) {
        if (dbFile == null) {
            return;
        }
        if (!outDbFiles.contains(dbFile.getAbsolutePath())) {
            outDbFiles.add(dbFile.getAbsolutePath());
        }
        if (FlipperUtil.context != null) {
            FlipperUtil.context.getSharedPreferences("flipper", Context.MODE_PRIVATE).edit().putString("db", new Gson().toJson(outDbFiles)).apply();
        }
    }

    static Object interceptGetDatabaseFiles(ProceedJoinPoint joinPoint) throws Throwable {
        if (!parsed) {
            parsed = true;
            String s = FlipperUtil.context.getSharedPreferences("flipper", Context.MODE_PRIVATE).getString("db", "");
            if (!TextUtils.isEmpty(s)) {
                try {
                    outDbFiles = new Gson().fromJson(s, new TypeToken<List<String>>() {
                    }.getType());
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }

        String methodName = joinPoint.getTargetMethod().getName();
        Log.v(TAG, "method begin:" + methodName);
        long begin = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            if (result instanceof List) {
                List datas = (List) result;
                List<File> files = new ArrayList<>();
                for (String outDbFile : outDbFiles) {
                    boolean contains = false;
                    for (Object data : datas) {
                        if (data instanceof File) {
                            File file = (File) data;
                            if (file.getAbsolutePath().equals(outDbFile)) {
                                contains = true;
                                break;
                            }
                        }
                    }
                    if (!contains) {
                        File file = new File(outDbFile);
                        if (file.exists() && file.length() > 0) {
                            files.add(file);
                        }
                    }
                }
                if (files.size() > 0) {
                    datas.addAll(files);
                    Log.v(TAG, "添加n个外部数据库:" + files.size());
                }
                Log.v(TAG, "数据库总个数:" + datas.size());
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        long duration = System.currentTimeMillis() - begin;
        Log.v(TAG, joinPoint.getTarget() + "." + methodName + "  耗时:" + duration + "ms");

        return result;
    }
}

@AndroidAopMatchClassMethod(
        targetClassName = "com.facebook.flipper.plugins.databases.impl.DefaultSqliteDatabaseProvider",
        methodName = {"getDatabaseFiles"},
        type = MatchType.SELF
)
class DbGetDatabaseFilesMatch implements MatchClassMethod {
    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return DBAspect.interceptGetDatabaseFiles(joinPoint);
    }
}
