package com.hss01248.dokit.btns;

import android.content.DialogInterface;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.GsonBuilder;
import com.hss01248.dokit.R;
import com.hss01248.dokit.parts.ICustomButton;

import java.lang.reflect.Field;
import java.util.Arrays;
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
        CharSequence[] items = new CharSequence[]{"BuildConfig","Build","Settings.System",
                "Settings.Secure","Settings.Global","Settings All","adb getprop"};
        AlertDialog dialog = new AlertDialog.Builder(ActivityUtils.getTopActivity())
                .setTitle("选择你要显示的信息")
                .setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        choosen[0] = which;
                        if(choosen[0] ==0){
                            showBuildConfig();
                        }else if(choosen[0] ==1){
                            showBuild();
                        }else  if(choosen[0] ==2){
                            showSettingsSystem();
                        }else  if(choosen[0] ==3){
                            showSettingsSecure();
                        }else  if(choosen[0] ==4){
                            showSettingsGlobal();
                        }else  if(choosen[0] ==5){
                            showSettingsAll();
                        }else  if(choosen[0] ==6){
                            showPropertiesAll();
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

    private void showPropertiesAll() {

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
                showMsg("getprop All",result);
            }

            @Override
            public void onFail(Throwable t) {
                super.onFail(t);
                showMsg("getprop All",t.getMessage());
            }
        });



    }

    public static String[] getProps() {
        MyShellUtils.CommandResult commandResult = MyShellUtils.execCmd("getprop", false);
        return commandResult != null && !TextUtils.isEmpty(commandResult.successMsg) ? commandResult.successMsg.split("\n") : new String[0];
    }

    private void showSettingsAll() {
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

        showMsg("Settings All",new GsonBuilder().setPrettyPrinting().create().toJson(map));
    }

    private void showSettingsGlobal() {
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
        showMsg("Settings.Global",new GsonBuilder().setPrettyPrinting().create().toJson(map));
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
    private void showSettingsSecure() {
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

    private void showSettingsSystem() {
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
        showMsg("Settings.System",new GsonBuilder().setPrettyPrinting().create().toJson(map));
    }

    private void showBuild() {
        Map map = new TreeMap();
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object object = field.get(null);
                if(object instanceof String[]){
                    String[] arr = (String[]) object;
                    map.put(field.getName(), Arrays.toString(arr));
                }else{
                    map.put(field.getName(), object+"");
                }

            } catch (Exception e) {
                map.put("exception", e.getMessage());
                //e.printStackTrace();
            }
        }
        showMsg("Build",new GsonBuilder().setPrettyPrinting().create().toJson(map));
    }

    private static void showBuildConfig() {
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
        showMsg("BuildConfig信息",msg);
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
