package com.hss01248.flipperdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.blankj.utilcode.util.ThreadUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hss01248.flipper.DBAspect;
import com.hss01248.image.dataforphotoselet.ImgDataSeletor;
import com.hss01248.media.metadata.FileTypeUtil;

import org.devio.takephoto.wrap.TakeOnePhotoListener;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new OkHttpClient.Builder()
                //.addInterceptor(new MyAppHelperInterceptor())
                .retryOnConnectionFailure(false).build();

        XXPermissions.with(this).permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        DBAspect.addDB(getFile("testaccount3.db"));
                        DBAspect.addDB(getFile("imgdownload.db"));
                    }
                });
    }


    private File getFile(String name){
        String dbDir=android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        dbDir += "/.yuv/databases";//数据库所在目录
        String dbPath = dbDir+"/"+name;//数据库路径
        File file = new File(dbPath);
        return file;
    }


    OkHttpClient client;
    ExecutorService executorService = Executors.newCachedThreadPool();



    public void http(View view) {

        executorService.execute(new Runnable() {
                @Override
                public void run() {
                    client.newCall(new Request.Builder().url("https://www.baidu.com/path2").build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.d("dd",response.toString());

                        }
                    });

                    try {
                        JSONObject object = new JSONObject();
                        object.put("name","yasuo")
                                .put("id1",2812351817172648966L)
                                .put("idstr","2812351817172648966");
                        String str = object.toString();
                        RequestBody body = RequestBody.create(MediaType.parse("application/json"),str.getBytes());

                        OkHttpClient build = new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                                .build();
                       // build.networkInterceptors().add(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
                        build.newCall(new Request.Builder().url("https://www.baidu.com/biglong").post(body).build()).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Log.d("dd",response.toString());

                            }
                        });



                    }catch (Throwable throwable){
                        throwable.printStackTrace();
                    }


                }
            });
        }

    public void imgUpload(View view) {
        ImgDataSeletor.startPickOneWitchDialog(this, new TakeOnePhotoListener() {
            @Override
            public void onSuccess(String path) {
                upload(path);
            }

            @Override
            public void onFail(String path, String msg) {

            }

            @Override
            public void onCancel() {

            }
        });
        //RequestBody body = RequestBody.create(MediaType.parse("image/jpeg"),)
    }

    private void upload(String path) {
        RequestBody body = RequestBody.create(MediaType.parse(FileTypeUtil.getMineType(path)),new File(path));
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                client.newCall(new Request.Builder().url("https://www.baidu.com/17/yui").put(body).build())
                        .enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {

                            }
                        });
            }
        });

    }

    public void crash(View view) {
        int i = 1/0;
    }

    public void post(View view) {
        EventBus.getDefault().post(new Event1(false));
    }

    public void postSticky(View view) {
        EventBus.getDefault().post(new Event1(true));
    }

    public void urlConnection(View view) {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Object>() {
            @Override
            public Object doInBackground() throws Throwable {
                URL url = new URL("http://baidu.com");
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(8000);
                urlConnection.setRequestProperty("key","value");
                InputStream inputStream = urlConnection.getInputStream();// 字节流
                Reader reader = new InputStreamReader(inputStream);//字符流
                BufferedReader bufferedReader = new BufferedReader(reader);// 缓存流
                StringBuilder result = new StringBuilder();;
                String temp;
                while ((temp = bufferedReader.readLine()) != null) {
                    result.append(temp);
                }
                Log.i("MainActivity-url", result.toString());

                if (reader!=null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream!=null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (bufferedReader!=null){
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (urlConnection != null){
                    urlConnection.disconnect();
                }
                return null;
            }

            @Override
            public void onSuccess(Object result) {

            }
        });

    }
}