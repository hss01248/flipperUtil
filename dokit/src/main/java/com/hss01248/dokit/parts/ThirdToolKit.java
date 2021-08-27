package com.hss01248.dokit.parts;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.didichuxing.doraemonkit.kit.AbstractKit;
import com.didichuxing.doraemonkit.kit.Category;
import com.hss01248.dokit.R;

import java.util.ArrayList;
import java.util.List;

public class ThirdToolKit extends AbstractKit {
    boolean show;
    @Override
    public int getCategory() {
        return Category.TOOLS;
    }

    @Override
    public int getName() {
        if(show){
            return R.string.testkit_third;
        }else {
            return R.string.testkit_third;
        }

    }
    @Override
    public String innerKitId() {
        return "dokit_sdk_tool_third_party_tools";
    }

    static Context context;
    @Override
    public int getIcon() {
        return R.drawable.dk_add_shape;
    }

    @Override
    public void onClick(Context context) {
        ThirdToolKit.context = context.getApplicationContext();
        String[] names = getNames(getApks());
        new AlertDialog.Builder(context)
                .setSingleChoiceItems(names, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            IThirdPartyApk apk = getApks().get(which);
                            openApp(apk.pkgName(),apk.downloadUrl());
                        }catch (Throwable e){
                            e.printStackTrace();
                        }

                    }
                }).show();
    }

    private String[] getNames(List<IThirdPartyApk> thirdPartyApks) {
        String[] names = new String[thirdPartyApks.size()];
        for (int i = 0; i < thirdPartyApks.size(); i++) {
            names[i] = thirdPartyApks.get(i).name();
        }
        return names;
    }

    @Override
    public void onAppInit(Context context) {


    }

    static List<IThirdPartyApk> getApks(){
        List<IThirdPartyApk> apks = new ArrayList<>();
        IThirdPartyApk apk0 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "aptoide(第三方应用商店,下载谷歌商店上架的应用)";
            }

            @Override
            public String pkgName() {
                return "com.itemstudio.castro";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return "https://en.aptoide.com/";
            }
        };
        IThirdPartyApk apk1 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "castro(手机硬件软件信息查看)";
            }

            @Override
            public String pkgName() {
                return "com.itemstudio.castro";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return null;
            }
        };


        IThirdPartyApk apk2 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "skit(手机上安装的app列表信息查看)";
            }

            @Override
            public String pkgName() {
                return "com.pavelrekun.skit";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return null;
            }
        };




        IThirdPartyApk apk4 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "cellular-Z(最牛的wifi和无线信号查看)";
            }

            @Override
            public String pkgName() {
                return "make.more.r2d2.cellular_z";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return null;
            }
        };

        IThirdPartyApk apk5 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "httpCanary(最牛的第三方抓包app)";
            }

            @Override
            public String pkgName() {
                return "com.guoshi.httpcanary";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return null;
            }
        };

        IThirdPartyApk apk6 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "开发工具箱(开发者选项辅助点击)";
            }

            @Override
            public String pkgName() {
                return "com.zengalv.devhelper";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return null;
            }
        };

        IThirdPartyApk apk7 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "httpInfo(http测试工具)";
            }

            @Override
            public String pkgName() {
                return "com.example.httpinfo";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return "https://github.com/guxiaonian/HttpInfo/blob/master/app/release/app-release.apk?raw=true";
            }
        };

        IThirdPartyApk apk8 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "mobileInfo(手机软硬件信息，包含判断是否模拟器)";
            }

            @Override
            public String pkgName() {
                return "com.mobile.mobileinfo";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return "https://github.com/guxiaonian/MobileInfo/blob/master/app/release/app-release.apk?raw=true";
            }
        };

        IThirdPartyApk apk9 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "show Java(apk反编译)";
            }

            @Override
            public String pkgName() {
                return "com.njlabs.showjava";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return null;
            }
        };

        IThirdPartyApk apk10 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "topActivity(查看顶层activity，不用辅助功能权限)";
            }

            @Override
            public String pkgName() {
                return "com.willme.topactivity";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return null;
            }
        };
        IThirdPartyApk apk11 = new IThirdPartyApk() {
            @Override
            public String name() {
                return "virtualUETool(张珂同学开发的查看任意app布局元素的工具)";
            }

            @Override
            public String pkgName() {
                return "io.virtualapp268";
            }

            @Nullable
            @Override
            public String downloadUrl() {
                return "https://github.com/zhangke3016/VirtualUETool/blob/master/app-release.apk?raw=true";
            }
        };



        apks.add(apk0);
        apks.add(apk1);
        apks.add(apk8);
        apks.add(apk2);
        apks.add(apk9);
        apks.add(apk4);
        apks.add(apk5);
        apks.add(apk6);
        apks.add(apk7);

        apks.add(apk10);
        apks.add(apk11);


        return apks;
    }

    public static void openApp(String pkgName, String backupUrl) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            openApplicationMarket(pkgName, backupUrl);
        }
    }

    private static void openApplicationMarket(String packageName, String backupUrl) {
        if (!TextUtils.isEmpty(backupUrl)) {
            openLinkBySystem(backupUrl);
            return;
        }
        try {
            String str = "market://details?id=" + packageName;
            Intent localIntent = new Intent(Intent.ACTION_VIEW);
            localIntent.setData(Uri.parse(str));
            localIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(localIntent);
        } catch (Exception e) {
            // 打开应用商店失败 可能是没有手机没有安装应用市场
            e.printStackTrace();
            Toast.makeText(context, "打开应用商店失败", Toast.LENGTH_SHORT).show();
            // 调用系统浏览器进入商城
            if (TextUtils.isEmpty(backupUrl)) {
                backupUrl = "https://play.google.com/store/apps/details?id=" + packageName;
            }
            openLinkBySystem(backupUrl);
        }
    }

    private static void openLinkBySystem(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
