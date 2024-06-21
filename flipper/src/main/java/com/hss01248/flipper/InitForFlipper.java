package com.hss01248.flipper;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.startup.Initializer;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class InitForFlipper implements Initializer<String> {
    @Override
    public String create(Context context) {
        Log.d("init","FlipperUtil.init start");
        FlipperUtil.init(context,true,null);

        addTestDB(context);
        return "flipper";
    }

    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        List<Class<? extends Initializer<?>>> list =  new ArrayList<>();
        try {
            list.add((Class<? extends Initializer<?>>) Class.forName("com.hss01248.flipper_leakcanary.LeakUtil"));
        } catch (Throwable e) {
            LogUtils.w(e);
        }
        return list;
    }


    /**
     * 需求是读取sdcard上txt文件
     * Android13（targetSDK = 33）上取消了WRITE_EXTERNAL_STORAGE，READ_EXTERNAL_STORAGE权限。
     * 取而代之的是READ_MEDIA_VIDEO，READ_MEDIA_AUDIO，READ_MEDIA_IMAGES权限
     * 测试发现，即便动态申请上面三个权限，仍旧无法读取本地txt文件
     *
     * 作者：OpenGL
     * 链接：https://juejin.cn/post/7283152332622610492
     * 来源：稀土掘金
     * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
     * @param context
     */
    private void addTestDB(Context context) {
        boolean canReadExt = false;
        //纯粹的读取权限,这样判断:
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
            canReadExt = Environment.isExternalStorageManager();
        }else {
            canReadExt = PermissionUtils.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if(canReadExt){
            DBAspect.addDB(getFile("testaccount3.db"));
            DBAspect.addDB(getFile("imgdownload.db"));
        }else{
            LogUtils.i("没有READ_EXTERNAL_STORAGE");
        }
    }

    private File getFile(String name){
        String dbDir=android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        dbDir += "/.yuv/databases";//数据库所在目录
        String dbPath = dbDir+"/"+name;//数据库路径
        File file = new File(dbPath);
        return file;
    }
}
