package com.hss01248.dokit.btns;

import android.content.Context;
import android.content.Intent;

import com.glance.guolindev.Glance;
import com.glance.guolindev.ui.db.DBActivity;
import com.hss01248.dokit.InitForDokit;
import com.hss01248.dokit.R;
import com.hss01248.dokit.parts.ICustomButton;

public class DbDebugBtn extends ICustomButton {
    @Override
    public int getName() {
        return R.string.testkit_go_db;
    }

    @Override
    public int getIcon() {
        return R.drawable.tools_db;
    }

    @Override
    public void onAppInit(Context context) {
        super.onAppInit(context);
        Glance.INSTANCE.initialize(context);
        //todo 辣鸡,不能设置sd卡的数据库文件
              /* String str =  context.getSharedPreferences("glance_library_db_cache",Context.MODE_PRIVATE).getString("glance_library_cached_databases","[]");
               List<DBFile> dbFiles = new Gson().fromJson(str,new TypeToken<List<DBFile>>(){}.getType());
               boolean hasImgDownload = false;

               File file = new File(Environment.getExternalStorageDirectory(),".yuv/databases/imgdownload_copy.db");

                DBFile db = new DBFile(file.getName(),file.getAbsolutePath(),false,new Date(file.lastModified()));
                for (DBFile dbFile : dbFiles) {
                    if(dbFile.getPath().equals(file.getAbsolutePath())){
                        hasImgDownload = true;
                        break;
                    }
                }
                Log.w("db","has imgdownload_copy.db:"+hasImgDownload);
                if(!hasImgDownload){
                    dbFiles.add(db);
                    String json = new Gson().toJson(dbFiles);
                    context.getSharedPreferences("glance_library_db_cache",Context.MODE_PRIVATE).edit().putString("glance_library_cached_databases",json).commit();
                }
                Glance.INSTANCE.initialize(new ContextWrapper(context) {
                    @Override
                    public File getDatabasePath(String name) {
                        return file;
                    }
                });*/
    }

    @Override
    public void onClick() {
        // InitForDokit.goAppSettings();
        //Glance.INSTANCE.initialize();
        Intent intent = new Intent(InitForDokit.context, DBActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        InitForDokit.context.startActivity(intent);
    }
}
