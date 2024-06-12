package com.hss01248.dokit.btns;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.GsonBuilder;
import com.hss01248.dokit.R;
import com.hss01248.dokit.parts.ICustomButton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @Despciption todo
 * @Author hss
 * @Date 11/07/2022 14:28
 * @Version 1.0
 */
public class BuildTypeInfoDisplayBtn extends ICustomButton {
    @Override
    public int getName() {
        return R.string.tools_buildtype_info;
    }

    @Override
    public int getIcon() {
        return R.drawable.icon_info_testtool;
    }

    @Override
    public void onClick() {

        //https://www.cnblogs.com/shujk/p/14851329.html

        int[] choosen = new int[]{0};
        CharSequence[] items = new CharSequence[]{"apk-BuildConfig","硬件-Build","Settings.System",
                "Settings.Secure","Settings.Global","Settings All","linux命令-getprop",
        "android-application-config","android-当前activity-config",
                "apk-manifest-packageInfo","apk-manifest-applicationInfo","apk-manifest-activityInfo",
        "apk-manifest-serviceInfo","apk-manifest-providerInfo","apk-manifest-resolveInfo",
        "apk-manifest-metadata-application","apk-manifest-permissions"};
        AlertDialog dialog = new AlertDialog.Builder(ActivityUtils.getTopActivity())
                .setTitle("选择你要显示的软/硬件信息")
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choosen[0] = which;
                        if(choosen[0] ==0){
                            showBuildConfig(items[which].toString());
                        }else if(choosen[0] ==1){
                            showBuild(items[which].toString());
                        }else  if(choosen[0] ==2){
                            showSettingsSystem(items[which].toString());
                        }else  if(choosen[0] ==3){
                            showSettingsSecure(items[which].toString());
                        }else  if(choosen[0] ==4){
                            showSettingsGlobal(items[which].toString());
                        }else  if(choosen[0] ==5){
                            showSettingsAll(items[which].toString());
                        }else  if(choosen[0] ==6){
                            showPropertiesAll(items[which].toString());
                        }else  if(choosen[0] ==7){
                            showAppConfig(items[which].toString());
                        }else  if(choosen[0] ==8){
                            showActivityConfig(items[which].toString());
                        }else  if(choosen[0] ==9){
                            showManifestPackageInfo(items[which].toString());
                        }else  if(choosen[0] ==10){
                            showApplicationInfo(items[which].toString());
                        }else  if(choosen[0] ==11){
                            showActivityInfo(items[which].toString());
                        }else  if(choosen[0] ==12){
                            showServiceInfo(items[which].toString());
                        }else  if(choosen[0] ==13){
                            showProviderInfo(items[which].toString());
                        }else  if(choosen[0] ==14){
                            showResolveInfo(items[which].toString());
                        }else  if(choosen[0] ==15){
                            showMetadatas(items[which].toString());
                        }else  if(choosen[0] ==16){
                            showPermissions(items[which].toString());
                        }
                    }
                })
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        dialog.show();
    }

    private void showMetadatas(String string) {
        try {
            showMsg2(string,Utils.getApp().getPackageManager().getApplicationInfo
                    (AppUtils.getAppPackageName(), PackageManager.GET_META_DATA).metaData);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.w(e);
        }
    }

    private void showPermissions(String string) {

        try {
            PackageInfo packageInfo = Utils.getApp().getPackageManager().getPackageInfo(AppUtils.getAppPackageName(), PackageManager.GET_PERMISSIONS);
            Map<String,Boolean> map = new TreeMap<>();
            for (String info : packageInfo.requestedPermissions) {
                String name = info.replace("android.permission.","").toLowerCase();
                map.put(name, PermissionUtils.isGranted(info));
            }
            showMsg2(string,map);
        } catch (Throwable e) {
            LogUtils.w(e);
            showMsg2(string,e.getMessage());
        }
    }

    private void showResolveInfo(String string) {

    }

    private void showProviderInfo(String string) {

        try {
            PackageInfo packageInfo = Utils.getApp().getPackageManager().getPackageInfo(AppUtils.getAppPackageName(), PackageManager.GET_PROVIDERS);
            for (ProviderInfo service : packageInfo.providers) {
                service.applicationInfo = null;
            }
            showMsg2(string,packageInfo.providers);
        } catch (Throwable e) {
            LogUtils.w(e);
        }
    }

    private void showServiceInfo(String string) {
        try {
            PackageInfo packageInfo = Utils.getApp().getPackageManager().getPackageInfo(AppUtils.getAppPackageName(), PackageManager.GET_SERVICES);
            for (ServiceInfo service : packageInfo.services) {
                service.applicationInfo = null;
            }
            showMsg2(string,packageInfo.services);
        } catch (PackageManager.NameNotFoundException e) {
            LogUtils.w(e);
        }
    }

    private void showActivityInfo(String string) {
        try {
            PackageInfo packageInfo = Utils.getApp().getPackageManager().getPackageInfo(AppUtils.getAppPackageName(), PackageManager.GET_ACTIVITIES);
            for (ActivityInfo service : packageInfo.activities) {
                service.applicationInfo = null;
            }
            showMsg2(string,packageInfo.activities);
        } catch (Throwable e) {
            LogUtils.w(e);
        }
    }

    private void showApplicationInfo(String string) {
        try {
            showMsg2(string,Utils.getApp().getPackageManager().getApplicationInfo(AppUtils.getAppPackageName(), 0));
        } catch (Throwable e) {
            LogUtils.w(e);
        }
    }

    private void showManifestPackageInfo(String string) {
        try {
            PackageInfo packageInfo = Utils.getApp().getPackageManager().getPackageInfo(AppUtils.getAppPackageName(), 0);
            showMsg2(string,packageInfo);
        } catch (Throwable e) {
            LogUtils.w(e);
        }
    }

    private void showAppConfig(String title) {
        Configuration appConfiguration = Utils.getApp().getResources().getConfiguration();
         String msg = GsonUtils.getGson("nice").toJson(appConfiguration);
         showMsg(title,msg);
    }
    private void showActivityConfig(String title) {
        Configuration appConfiguration = ActivityUtils.getTopActivity().getResources().getConfiguration();
        Activity topActivity = ActivityUtils.getTopActivity();
        Map map = new TreeMap();
        map.put("config",appConfiguration);
        List<String> frags = new ArrayList<>();
        map.put("fragments",frags);
        if(topActivity instanceof FragmentActivity){
            FragmentActivity fragmentActivity = (FragmentActivity) topActivity;
            List<Fragment> fragments = fragmentActivity.getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                frags.add(fragment.toString());
            }
        }
        String msg = GsonUtils.getGson("nice").toJson(map);
        showMsg(title+"("+ActivityUtils.getTopActivity().getClass().getName()+")",msg);
    }



    private void showPropertiesAll(String title) {

        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<String>() {
            @Override
            public String doInBackground() throws Throwable {
                String[] arr = getProps();
                Map map = new TreeMap();
                if(arr.length >0){
                    int i = 0;
                    for (String s : arr) {
                        if(s.contains(":")){
                            String[] split = s.split(":");
                           String key =  split[0].replace("[","").replace("]","");
                           String value = split[1].replace("[","").replace("]","");
                            map.put(key,value);
                        }else {
                            map.put(i+"",s);
                        }
                        i++;
                    }

                }else {
                    map.put("reason","arr length is 0");
                }
                return new GsonBuilder().setPrettyPrinting().create().toJson(map);
            }

            @Override
            public void onSuccess(String result) {
                showMsg(title,result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                showMsg(title,t.getMessage());
            }
        });



    }

    public static String[] getProps() {
        MyShellUtils.CommandResult commandResult = MyShellUtils.execCmd("getprop", false);
        return commandResult != null && !TextUtils.isEmpty(commandResult.successMsg) ? commandResult.successMsg.split("\n") : new String[0];
    }

    private void showSettingsAll(String title) {
        Map map = new TreeMap();

        Field[] fields = Settings.Global.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object object = field.get(null);
                if(object instanceof String){
                    String key = (String) object;
                    map.put(key,Settings.Global.getString(Utils.getApp().getContentResolver() , key));
                }
            } catch (Exception e) {
                map.put("exception", e.getMessage());
            }
        }

        Field[] fields2 = Settings.Secure.class.getDeclaredFields();
        for (Field field : fields2) {
            try {
                field.setAccessible(true);
                Object object = field.get(null);
                if(object instanceof String){
                    String key = (String) object;
                    map.put(key,Settings.Secure.getString(Utils.getApp().getContentResolver() , key));
                }
            } catch (Exception e) {
                map.put("exception", e.getMessage());
            }
        }

        Field[] fields3 = Settings.System.class.getDeclaredFields();
        for (Field field : fields3) {
            try {
                field.setAccessible(true);
                Object object = field.get(null);
                if(object instanceof String){
                    String key = (String) object;
                    map.put(key,Settings.System.getString(Utils.getApp().getContentResolver() , key));
                }
            } catch (Exception e) {
                map.put("exception", e.getMessage());
            }
        }

        showMsg(title,new GsonBuilder().setPrettyPrinting().create().toJson(map));
    }

    private void showSettingsGlobal(String title) {
        //Settings.Global.getInt(getActivity().getContentResolver(),“xxx.xxx”, 0)
        Map map = new TreeMap();
        Field[] fields = Settings.Global.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object object = field.get(null);
                if(object instanceof String){
                    String key = (String) object;
                    map.put(key,Settings.Global.getString(Utils.getApp().getContentResolver() , key));
                }
            } catch (Exception e) {
                map.put("exception", e.getMessage());
            }
        }
        showMsg(title,new GsonBuilder().setPrettyPrinting().create().toJson(map));
    }

    //当需要获得当前wifi状态的值，调用已封装的方法如下：
    //　　Settings.Secure.getInt(getContentResolver() , Settings.Secure.WIFI_ON);
    //　　修改wifi状态只需要调用对应的setInt方法就可以实现。
    //
    //　　当需要获得当前时间日期自动获取，调用如下：
    //　　Settings.System.getInt(getContentResolver() , "auto_time");
    //　　修改也是调用对应的setInt方法。
    //对于上面通过getInt获得的字段，其实是在初始获得数据库数值的时候，
    // 首先是有getString方法将数据库数据保留，然后在integer.parseInt将数据转换成int类型。
    private void showSettingsSecure(String title) {
        Map map = new TreeMap();
        Field[] fields = Settings.Secure.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object object = field.get(null);
                if(object instanceof String){
                    String key = (String) object;
                    map.put(key,Settings.Secure.getString(Utils.getApp().getContentResolver() , key));
                }
            } catch (Exception e) {
                map.put("exception", e.getMessage());
            }
        }
        showMsg("Settings.Secure",new GsonBuilder().setPrettyPrinting().create().toJson(map));
    }

    private void showSettingsSystem(String title) {
        Map map = new TreeMap();
        Field[] fields = Settings.System.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object object = field.get(null);
                if(object instanceof String){
                    String key = (String) object;
                    map.put(key,Settings.System.getString(Utils.getApp().getContentResolver() , key));
                }
            } catch (Exception e) {
                map.put("exception", e.getMessage());
            }
        }
        showMsg(title,new GsonBuilder().setPrettyPrinting().create().toJson(map));
    }

    private void showBuild(String title) {
        Map map = new TreeMap();
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object object = field.get(null);
                if(object instanceof String[]){
                    String[] arr = (String[]) object;
                    map.put(field.getName().toLowerCase(), Arrays.toString(arr));
                }else{
                    map.put(field.getName().toLowerCase(), object+"");
                }

            } catch (Exception e) {
                map.put("exception", e.getMessage());
                //e.printStackTrace();
            }
        }
        showMsg(title,new GsonBuilder().setPrettyPrinting().create().toJson(map));
    }

    private static void showBuildConfig(String title) {
        Class buildConfig = buildConfig();
        String msg = "BuildConfig信息未能通过反射得到";
        if(buildConfig != null){
            Field[] declaredFields = buildConfig.getDeclaredFields();
            if(declaredFields != null){
                msg = buildConfig.getName()+":\n\n";
                for (Field field : declaredFields) {
                    field.setAccessible(true);
                    try {
                        msg += (field.getName().toLowerCase()+": "+field.get(buildConfig)+"\n");
                    }catch (Throwable throwable){
                        LogUtils.w(throwable);
                    }
                }
            }
        }
        showMsg(title,msg);
    }

    private static void showMsg2(String title,Object msg){
        showMsg(title,GsonUtils.getGson("nice").toJson(msg));
    }

    private static void showMsg(String title,String msg) {
        AlertDialog dialog = new AlertDialog.Builder(ActivityUtils.getTopActivity())
                .setTitle(title)
                .setMessage(msg)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog0) {
                WindowManager.LayoutParams attributes = dialog.getWindow().getAttributes();
                attributes.width = ScreenUtils.getScreenWidth();
                dialog.getWindow().setAttributes(attributes);
            }
        });
        dialog.show();

    }

    private static Class buildConfig() {
        try {
            String name0 = Utils.getApp().getPackageName() + ".BuildConfig";
            Class clazz = Class.forName(name0);
            return clazz;
        } catch (Throwable throwable) {
            LogUtils.d("getBuildType", "xx", throwable);
            String name = Utils.getApp().getClass().getName();
            name = name.substring(0, name.lastIndexOf("."));
            while (name.contains(".")){
                String config = name + ".BuildConfig";
                try {
                    Class clazz = Class.forName(config);
                    if(clazz != null){
                        return clazz;
                    }
                } catch (ClassNotFoundException e) {
                    LogUtils.i(e);
                }
                name = name.substring(0, name.lastIndexOf("."));
            }
            return null;
        }
    }
}
