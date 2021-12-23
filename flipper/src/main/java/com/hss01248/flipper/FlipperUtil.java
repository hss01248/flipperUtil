package com.hss01248.flipper;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.ddyos.flipper.mmkv.plugin.MMKVFlipperPlugin;
import com.facebook.flipper.android.AndroidFlipperClient;
import com.facebook.flipper.android.utils.FlipperUtils;
import com.facebook.flipper.core.FlipperClient;
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin;
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin;
import com.facebook.flipper.plugins.inspector.DescriptorMapping;
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin;



import com.facebook.flipper.plugins.network.BodyUtil;
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor;
import com.facebook.flipper.plugins.network.MyAppHelperInterceptor;
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin;
import com.facebook.flipper.plugins.network.RequestBodyParser;
import com.facebook.flipper.plugins.sandbox.SandboxFlipperPlugin;
import com.facebook.flipper.plugins.sandbox.SandboxFlipperPluginStrategy;
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin;
import com.facebook.soloader.SoLoader;
import com.hss01248.flipper.eventbus.EventBusLogger2FlipperPlugin;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import fr.afaucogney.mobile.flipper.BackStackFlipperPlugin;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;


/**
 官方限定,只能在debugable=true时使用: https://github.com/facebook/flipper/issues/1075
 */
public class FlipperUtil {
    static NetworkFlipperPlugin networkFlipperPlugin;
    static Context context;

    public static void addInterceptor(OkHttpClient.Builder builder){
        if(networkFlipperPlugin != null){
            builder.addNetworkInterceptor(new FlipperOkhttpInterceptor(networkFlipperPlugin));
        }else {
            Log.w("flipper","networkFlipperPlugin is null, you must call FlipperUtil.init() before addInterceptor()" );
        }
    }

    public static Interceptor getInterceptor(){
        if(networkFlipperPlugin != null){
            return new FlipperOkhttpInterceptor(networkFlipperPlugin);
        }
        Log.w("flipper","networkFlipperPlugin is null" );
        return null;
    }

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
                    throwable1.printStackTrace();
                }
            }
           /* LeakCanary.setConfig(new LeakCanary.Config().newBuilder()
                    .onHeapAnalyzedListener(new FlipperLeakListener())
                    .build());
            client.addPlugin(new LeakCanary2FlipperPlugin());*/

            client.start();
            OkhttpAspect.addHook(new OkhttpAspect.OkhttpHook() {
                @Override
                public void beforeBuild(OkHttpClient.Builder builder) {
                    List<Interceptor> interceptors = builder.networkInterceptors();
                    boolean hasFlipperPlugin = false;
                    for (Interceptor interceptor : interceptors) {
                        if(interceptor instanceof FlipperOkhttpInterceptor){
                            hasFlipperPlugin = true;
                            break;
                        }
                    }
                    if(!hasFlipperPlugin){
                       // builder.addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
                        builder.addNetworkInterceptor(new FlipperOkhttpInterceptor(networkFlipperPlugin));
                        //SslUtil.setAllCerPass(builder);
                       // builder.addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
                    }

                    List<Interceptor> interceptors1 = builder.interceptors();
                    boolean hasAppInterceptor = false;
                    for (Interceptor interceptor : interceptors1) {
                        if(interceptor instanceof MyAppHelperInterceptor){
                            hasAppInterceptor = true;
                            break;
                        }
                    }
                    if(!hasAppInterceptor){
                        interceptors1.add(0,new MyAppHelperInterceptor());
                    }
                }
            });
        }
    }

    private static void addPlugins(FlipperClient client, Application context) {
        client.addPlugin(new BackStackFlipperPlugin(context,true));
        //client.addPlugin(new LeakCanary2FlipperPlugin());
        client.addPlugin(new EventBusLogger2FlipperPlugin());
    }

    public static void addConfigBox(Context context,ConfigCallback callback){
        AndroidFlipperClient.getInstance(context).addPlugin(new SandboxFlipperPlugin(getStrategy(callback)));
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
