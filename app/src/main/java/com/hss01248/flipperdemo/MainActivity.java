package com.hss01248.flipperdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.flipper.plugins.network.BodyUtil;
import com.hss01248.image.dataforphotoselet.ImgDataSeletor;
import com.hss01248.media.metadata.FileTypeUtil;

import org.devio.takephoto.wrap.TakeOnePhotoListener;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
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
        client = new OkHttpClient.Builder().build();
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
                        object.put("name","yasuo").put("pw","md5xxxxx");
                        String str = object.toString();
                        RequestBody body = RequestBody.create(MediaType.parse("application/json"),str.getBytes());

                        new OkHttpClient.Builder().addInterceptor(
                                new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                                .build()
                                .newCall(new Request.Builder().url("https://www.baidu.com").post(body).build()).enqueue(new Callback() {
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
        BodyUtil.context = getApplicationContext();
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
}