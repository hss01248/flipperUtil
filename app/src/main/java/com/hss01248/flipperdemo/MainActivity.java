package com.hss01248.flipperdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.io.IOException;

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

    }



    public void http(View view) {

            new Thread(new Runnable() {
                @Override
                public void run() {
                    new OkHttpClient.Builder().build().newCall(new Request.Builder().url("https://www.baidu.com/path2").build()).enqueue(new Callback() {
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
            }).start();
        }

}