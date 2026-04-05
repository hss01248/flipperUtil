package com.hss01248.aop.network.hook;

import android.text.TextUtils;

import com.blankj.utilcode.util.LogUtils;
import com.flyjingfish.android_aop_annotation.ProceedJoinPoint;
import com.flyjingfish.android_aop_annotation.anno.AndroidAopMatchClassMethod;
import com.flyjingfish.android_aop_annotation.base.MatchClassMethod;
import com.flyjingfish.android_aop_annotation.enums.MatchType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import okhttp3.OkHttpClient;
import okhttp3.Protocol;

/**
 * by hss
 * data:2020/7/17
 * desc: OkHttpClient.Builder 构建切面（AndroidAOP MatchClassMethod，替代 AspectJ）
 */
public class OkhttpAspect {

    private static final String TAG = "OkhttpAspect";
    static int count;

    public static boolean printClientBuildStack = false;

    static List<OkhttpHook> hooks = new ArrayList<>();
    static List<String> ignoreThreadNameList = new ArrayList<>();

    static {
        ignoreThreadNameList.add("sm-http-");
    }

    public static void ignoreWhenThreadNameStartWith(String name) {
        if (!TextUtils.isEmpty(name)) {
            ignoreThreadNameList.add(name);
        }
    }

    public static void addHook(OkhttpHook hook) {
        hooks.add(hook);
        Collections.sort(hooks, new Comparator<OkhttpHook>() {
            @Override
            public int compare(OkhttpHook o1, OkhttpHook o2) {
                return o2.initOrder() - o1.initOrder();
            }
        });
    }

    static WeakHashMap<OkHttpClient, String> clientMap = new WeakHashMap<>();

    static Object interceptOkHttpClientBuild(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        long begin = System.currentTimeMillis();
        Object result = null;
        try {
            StackTraceElement[] elements = isRN();
            if (elements != null) {
                Object target = joinPoint.getTarget();
                if (target instanceof OkHttpClient.Builder) {
                    OkHttpClient.Builder builder = (OkHttpClient.Builder) target;
                    String name = Thread.currentThread().getName();
                    boolean ignore = false;
                    if (!TextUtils.isEmpty(name) && !ignoreThreadNameList.isEmpty()) {
                        for (String s : ignoreThreadNameList) {
                            if (name.startsWith(s)) {
                                LogUtils.iTag(TAG, "ignore okhttp hook because the thread name start with " + s + "--> " + name);
                                ignore = true;
                            }
                        }
                    }
                    if (!ignore) {
                        fixOkHttpBug(builder);
                        if (hooks.size() > 0) {
                            Iterator<OkhttpHook> iterator = hooks.iterator();
                            while (iterator.hasNext()) {
                                iterator.next().beforeBuild(builder);
                            }
                        }
                    }
                }
                count++;
            } else {
                LogUtils.vTag(TAG, "is RN dev socket connector!!! ignore , thead name: " + Thread.currentThread().getName());
            }

            result = joinPoint.proceed();
            if (elements != null && elements.length > 0 && result instanceof OkHttpClient) {
                OkHttpClient client = (OkHttpClient) result;
                clientMap.put(client, elements[0].toString());
            }
            long duration = System.currentTimeMillis() - begin;
            LogUtils.vTag(TAG, joinPoint.getTarget() + "." + methodName + "  耗时:" + duration + "ms,已构建常规okhttpclient个数:" + count);
            if (printClientBuildStack) {
                LogUtils.vTag(TAG, "okhttpClient信息:\n" + clientsInfo2());
            }

        } catch (Throwable throwable) {
            LogUtils.wTag(TAG, "构建okhttpclient失败", throwable);
        }

        return result;
    }

    public static void fixOkHttpBug(OkHttpClient.Builder builder) {
        builder.protocols(Arrays.asList(Protocol.HTTP_1_1));
    }

    private static String clientsInfo2() {
        StringBuilder builder = new StringBuilder();
        builder.append("client real count: ")
                .append(clientMap.size())
                .append("\n");
        for (OkHttpClient client : clientMap.keySet()) {
            builder.append(client)
                    .append(":\t")
                    .append(clientMap.get(client))
                    .append(":\t")
                    .append(clientInfo(client))
                    .append("\n");
        }
        return builder.toString();
    }

    private static String clientInfo(OkHttpClient client) {
        StringBuilder sb = new StringBuilder();
        ExecutorService executorService = client.dispatcher().executorService();
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) executorService;
            sb.append("activeThread:")
                    .append(executor.getActiveCount())
                    .append(", coreSize:")
                    .append(executor.getCorePoolSize())
                    .append(", poolSize:")
                    .append(executor.getPoolSize())
                    .append(", taskCount:")
                    .append(executor.getTaskCount())
                    .append(", CompletedTaskCount:")
                    .append(executor.getCompletedTaskCount());
        } else {
            sb.append(executorService);
        }
        return sb.toString();
    }

    private static StackTraceElement[] isRN() {
        Exception exception = new Exception("just show okhttpclient build stacks");

        StackTraceElement[] stackTraces = exception.getStackTrace();
        for (StackTraceElement stackTrace : stackTraces) {
            if (stackTrace.getClassName().contains("com.facebook.react.packagerconnection")) {
                return null;
            }
            if (stackTrace.getClassName().contains("com.facebook.react.devsupport")) {
                return null;
            }
        }
        StackTraceElement[] stackTraceElements1 = new StackTraceElement[stackTraces.length - 4];
        for (int i = 0; i < stackTraces.length - 4; i++) {
            stackTraceElements1[i] = stackTraces[i + 4];
        }
        exception.setStackTrace(stackTraceElements1);
        if (printClientBuildStack) {
            LogUtils.vTag(TAG, "clientBuilder.build() call stacks", exception);
        }
        return stackTraceElements1;
    }

    public interface OkhttpHook {

        /**
         * 因为有些情况下会调用client.newBuilder().builder(),如果加拦截器,要自行判重
         */
        void beforeBuild(OkHttpClient.Builder builder);

        default int initOrder() {
            return 0;
        }
    }
}

/**
 * 匹配 okhttp3.OkHttpClient.Builder#build()，织入逻辑见 {@link OkhttpAspect#interceptOkHttpClientBuild}
 */
@AndroidAopMatchClassMethod(
        targetClassName = "okhttp3.OkHttpClient$Builder",
        methodName = {"build"},
        type = MatchType.SELF
)
class OkhttpClientBuilderBuildMatch implements MatchClassMethod {

    @Override
    public Object invoke(ProceedJoinPoint joinPoint, String methodName) throws Throwable {
        return OkhttpAspect.interceptOkHttpClientBuild(joinPoint, methodName);
    }
}
