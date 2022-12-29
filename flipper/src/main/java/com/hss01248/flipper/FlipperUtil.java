package com.hss01248.flipper;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.ddyos.flipper.mmkv.plugin.MMKVFlipperPlugin;
import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.android.utils.FlipperUtils;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.core.FlipperPlugin;
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin;
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin;
import com.facebook.flipper.plugins.network.RequestBodyParser;
import com.facebook.flipper.plugins.sandbox.SandboxFlipperPlugin;
import com.facebook.flipper.plugins.sandbox.SandboxFlipperPluginStrategy;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.soloader.SoLoader;
import com.hss01248.aop.network.hook.OkhttpAspect;
import com.hss01248.flipper.eventbus.EventBusLogger2FlipperPlugin;
import com.hss01248.flipper.http.OkhttpHookForFlipper;
import com.hss01248.flipper_collector.FlipperCollector;
import com.hss01248.network.body.meta.interceptor.BodyUtil;
import com.hss01248.network.body.meta.interceptor.BodyUtil2;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 官方限定,只能在debugable=true时使用: https://github.com/facebook/flipper/issues/1075
 */
public class FlipperUtil {
    public static NetworkFlipperPlugin getNetworkFlipperPlugin() {
        return networkFlipperPlugin;
    }

    static NetworkFlipperPlugin networkFlipperPlugin;
    static Context context;


    public static void setRequestBodyParser(RequestBodyParser requestBodyParser) {
        FlipperOkhttpInterceptor.setRequestBodyParser(requestBodyParser);
    }

    /**
     * 开启全部插件: network,database,shareprefences,leakcanary,crashReporter,layout inspector
     * @param app
     * @param enable 是否开启的开关
     * @param callback  sandbox对应的开关. 用于一些配置项,直接在flipper的操作界面上更改配置
     */
     static void init(Context app, boolean enable, ConfigCallback callback){
         context = app;
         FliSpUtil.init(app);
         //ProxyUrlAndChromeUtil.proxyUrlConnection();
         /*try {
             LeakCanary.Config config =
                     new LeakCanary.Config.Builder(LeakCanary.getConfig())
                             .onHeapAnalyzedListener(new FlipperLeakListener()).build();
             LeakCanary.setConfig(config);
         }catch (Throwable throwable){
             throwable.printStackTrace();
         }*/


        SoLoader.init(app, false);
        if (enable && FlipperUtils.shouldEnableFlipper(app)) {
            final FlipperClient client = AndroidFlipperClient.getInstance(app);
            BodyUtil.context = app;

            networkFlipperPlugin = new NetworkFlipperPlugin();
            client.addPlugin(networkFlipperPlugin);
            client.addPlugin(CrashReporterPlugin.getInstance());
            client.addPlugin(new DatabasesFlipperPlugin(app));

            client.addPlugin(new SharedPreferencesFlipperPlugin(app));
            final SandboxFlipperPluginStrategy strategy = getStrategy(callback); // Your strategy goes here
            client.addPlugin(new SandboxFlipperPlugin(strategy));
            client.addPlugin(new InspectorFlipperPlugin(app, DescriptorMapping.withDefaults()));


            addPlugins(client,(Application) app);

            try {
                client.addPlugin(new MMKVFlipperPlugin());
            }catch (Throwable throwable){
                throwable.printStackTrace();
                try {
                    if(throwable.getMessage().contains("Call MMKV.initialize() first")){
                        Class clazz = Class.forName("com.tencent.mmkv.MMKV");
                        Method method = clazz.getDeclaredMethod("initialize", Context.class);
                        method.invoke(clazz,app);
                        client.addPlugin(new MMKVFlipperPlugin());
                    }
                }catch (Throwable throwable1){
                    Log.w("flipper","mmkv not found");
                    //throwable1.printStackTrace();
                }
            }
           /* LeakCanary.setConfig(new LeakCanary.Config().newBuilder()
                    .onHeapAnalyzedListener(new FlipperLeakListener())
                    .build());
            client.addPlugin(new LeakCanary2FlipperPlugin());*/

            client.start();
            OkhttpAspect.addHook(new OkhttpHookForFlipper());
        }
    }

    private static void addPlugins(FlipperClient client, Application context) {
        //client.addPlugin(new BackStackFlipperPlugin(context,true));
        //client.addPlugin(new LeakCanary2FlipperPlugin());
        client.addPlugin(new EventBusLogger2FlipperPlugin());
        List<FlipperPlugin> plugins = FlipperCollector.getPlugins();
        LogUtils.i(plugins);

        if(plugins!= null && !plugins.isEmpty()){
            for (FlipperPlugin plugin : plugins) {
                if(plugin != null){
                    client.addPlugin(plugin);
                }else {
                    LogUtils.w("plugin is null");
                }
            }
        }
    }

    public static void addConfigBox(Context context,ConfigCallback callback){
        AndroidFlipperClient.getInstance(context).addPlugin(new SandboxFlipperPlugin(getStrategy(callback)));
    }

    public static void attachBaseContext(Context base) {
        BodyUtil2.attachBaseContext(base);
    }

    /**
     * From looking at the code, I believe you just need to implement a SandboxFlipperPluginStrategy, and pass it in, when constructing the plugin.
     *
     * setSandbox should override the sandbox within your app. It will be called on your strategy when the user enters a string as their sandbox. What this does is technically up to the implementation, but it could for example, set the base url for outgoing requests from the app to staging.myapp.com for example. How this is done will depends on your app.
     *
     * getKnownSandboxes should return a map of predefined options, keyed by name. For example, you could provide this as a map:
     *
     * {
     * test: test.myapp.com,
     * staging: staging.myapp.com,
     * production: prod.myapp.com
     * }
     * to let the user select from a list without typing.
     *
     * Feel free to update the docs:
     * https://github.com/facebook/flipper/blob/master/docs/setup/sandbox-plugin.md
     * @return
     * @param callback
     */
    private static SandboxFlipperPluginStrategy getStrategy(ConfigCallback callback) {
        return new SandboxFlipperPluginStrategy() {
            @Override
            public Map<String, String> getKnownSandboxes() {
                Map<String, String> map = new HashMap();
                if(callback != null){
                    if(callback.presetConfigMap() != null){
                        map.putAll(callback.presetConfigMap());
                    }
                }
                return map;
            }

            @Override
            public void setSandbox(String sandbox) {
                if(callback != null){
                    callback.onConfigSelected(sandbox);
                }


            }
        };
    }
}
