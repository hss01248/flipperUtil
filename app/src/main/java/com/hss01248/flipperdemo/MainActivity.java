package com.hss01248.flipperdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.facebook.flipper.plugins.network.NetworkReporter;
import com.facebook.flipper.plugins.network.RequestBodyParser;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hss01248.flipper.DBAspect;
import com.hss01248.flipper.FlipperUtil;
import com.hss01248.http.ConfigInfo;
import com.hss01248.http.HttpUtil;
import com.hss01248.http.callback.MyNetCallback;
import com.hss01248.http.config.FileDownlodConfig;
import com.hss01248.http.response.ResponseBean;
import com.hss01248.image.dataforphotoselet.ImgDataSeletor;
import com.hss01248.media.metadata.FileTypeUtil;
import com.hss01248.network.chucker.OkhttpHookForChucker;

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
import java.util.Map;
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
import okio.Buffer;

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
                    /*client.newCall(new Request.Builder().url("https://www.baidu.com/path2").build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.d("dd",response.toString());

                        }
                    });*/

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

    /**
     * 测试用视频下载
     *
     * 1、地址：http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4  1分钟
     * 2、地址：http://vjs.zencdn.net/v/oceans.mp4
     * 3、地址：https://media.w3.org/2010/05/sintel/trailer.mp4  52秒
     * 4、http://mirror.aarnet.edu.au/pub/TED-talks/911Mothers_2010W-480p.mp4   10分钟
     * @param view
     */
    public void downloadLargeFile(View view) {
        //String url = "https://media.w3.org/2010/05/sintel/trailer.mp4";
        String url = "https://services.gradle.org/distributions/gradle-6.5-rc-1-docs.zip";
        ProgressDialog dialog  = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);

        HttpUtil.download(url)
                .setFileDownlodConfig(FileDownlodConfig.newBuilder()
                        .fileDir(getExternalFilesDir("down").getAbsolutePath())
                        .build())
                .callback(new MyNetCallback<ResponseBean<FileDownlodConfig>>() {
                    @Override
                    public void onSuccess(ResponseBean<FileDownlodConfig> response) {
                        dialog.dismiss();
                        ToastUtils.showShort("success");

                    }

                    @Override
                    protected void onStart() {
                        super.onStart();
                        dialog.show();
                    }

                    @Override
                    public void onProgressChange(long transPortedBytes, long totalBytes, ConfigInfo info) {
                        super.onProgressChange(transPortedBytes, totalBytes, info);
                        //LogUtils.i("transPortedBytes:"+transPortedBytes+"--totalBytes:"+totalBytes);
                        String percent = transPortedBytes*100f/totalBytes+"%\n"+
                                ConvertUtils.byte2FitMemorySize(transPortedBytes,1)+"/"+ConvertUtils.byte2FitMemorySize(totalBytes,1);
                        dialog.setMessage(percent);

                    }

                    @Override
                    public void onError(String msgCanShow) {
                        dialog.dismiss();
                        ToastUtils.showLong(msgCanShow);

                    }
                });

    }

    public void reportException(View view) {
        OkhttpHookForChucker.getChuckerCollector().onError("dd",new RuntimeException("testxxxxxx"));
    }
}