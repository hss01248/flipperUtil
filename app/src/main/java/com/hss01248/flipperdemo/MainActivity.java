package com.hss01248.flipperdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
                    new OkHttpClient.Builder().build().newCall(new Request.Builder().url("https://www.baidu.com").build()).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            Log.d("dd",response.toString());

                        }
                    });

                }
            }).start();
        }

}