package com.hss01248.flipperdemo;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hss01248.http.ConfigInfo;
import com.hss01248.http.HttpUtil;
import com.hss01248.http.callback.MyNetCallback;
import com.hss01248.http.config.FileDownlodConfig;
import com.hss01248.http.response.ResponseBean;
import com.hss01248.image.dataforphotoselet.ImgDataSeletor;
import com.hss01248.media.metadata.FileTypeUtil;

import org.devio.takephoto.wrap.TakeOnePhotoListener;
import org.greenrobot.eventbus.EventBus;

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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSource;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_SSE = "MainActivity-SSE";
    /**
     * 模拟器访问宿主机：10.0.2.2。真机请改为与电脑同一局域网的 IP，例如 http://192.168.1.5:18080/sse
     */
    private static final String SSE_URL = "http://10.0.178.41:18080/sse";
    /** HTTPS 示例图,OkHttp 默认跟随重定向 */
    private static final String SAMPLE_IMAGE_URL = "https://picsum.photos/400/300";
    /**
     * 用于 {@link #httpDuplicateHeaders}: 需能访问外网; 不可用可改为 {@code https://www.baidu.com/} 等,
     * Flipper 里仍可观查发出的请求头。
     */
    private static final String DUPLICATE_HEADER_TEST_URL = "https://httpbin.org/get";
    /**
     * httpbin：查询串中重复同名参数会生成多条同名响应头，供 Flipper 校验 {@code convertHeader}。
     */
    private static final String DUPLICATE_RESPONSE_HEADER_TEST_URL =
            "https://httpbin.org/response-headers"
                    + "?X-Flipper-Dup-Resp=first&X-Flipper-Dup-Resp=second&X-Flipper-Dup-Resp=third";

    private Call sseCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new OkHttpClient.Builder()
                //.addInterceptor(new MyAppHelperInterceptor())
                .retryOnConnectionFailure(false).build();
        sseClient = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();



        XXPermissions.with(this).permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        //DBAspect.addDB(getFile("testaccount3.db"));
                       // DBAspect.addDB(getFile("imgdownload.db"));
                    }
                });
    }

    @Override
    protected void onDestroy() {
        if (sseCall != null) {
            sseCall.cancel();
            sseCall = null;
        }
        super.onDestroy();
    }

    private File getFile(String name){
        String dbDir=android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
        dbDir += "/.yuv/databases";//数据库所在目录
        String dbPath = dbDir+"/"+name;//数据库路径
        File file = new File(dbPath);
        return file;
    }


    OkHttpClient client;
    OkHttpClient sseClient;
    ExecutorService executorService = Executors.newCachedThreadPool();



    public void http(View view) {
        executorService.execute(() -> new HttpDemoTestHelper().runAllDemos());
    }

    /**
     * 使用 {@link Request.Builder#addHeader} 附加多条同名头（{@link Request.Builder#header} 会覆盖）。
     * 连接 Flipper 后打开 Network，请求里应看到三条 {@code X-Flipper-Dup-Test}，分别为 first / second / third。
     */
    public void httpDuplicateHeaders(View view) {
        Request request =
                new Request.Builder()
                        .url(DUPLICATE_HEADER_TEST_URL)
                        .addHeader("X-Flipper-Dup-Test", "first")
                        .addHeader("X-Flipper-Dup-Test", "second")
                        .addHeader("X-Flipper-Dup-Test", "third")
                        .get()
                        .build();
        client.newCall(request)
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showLong(
                                                        "请求失败(若访问不了 httpbin 可改 DUPLICATE_HEADER_TEST_URL): "
                                                                + e.getMessage()));
                            }

                            @Override
                            public void onResponse(Call call, Response response) {
                                try {
                                    final int code = response.code();
                                    runOnUiThread(
                                            () ->
                                                    ToastUtils.showShort(
                                                            "HTTP "
                                                                    + code
                                                                    + " — Flipper Network 中应见 3 条 X-Flipper-Dup-Test"));
                                } finally {
                                    response.close();
                                }
                            }
                        });
    }

    /**
     * 请求 httpbin {@code /response-headers}，服务端返回三条 {@code X-Flipper-Dup-Resp} 响应头。
     * Flipper Network 中应列出三条，值分别为 first / second / third。
     */
    public void httpDuplicateResponseHeaders(View view) {
        Request request = new Request.Builder().url(DUPLICATE_RESPONSE_HEADER_TEST_URL).get().build();
        client.newCall(request)
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(
                                        () ->
                                                ToastUtils.showLong(
                                                        "请求失败(若访问不了 httpbin 可改 DUPLICATE_RESPONSE_HEADER_TEST_URL): "
                                                                + e.getMessage()));
                            }

                            @Override
                            public void onResponse(Call call, Response response) {
                                try {
                                    final int code = response.code();
                                    int dupCount = response.headers().values("X-Flipper-Dup-Resp").size();
                                    Log.d(
                                            "MainActivity",
                                            "duplicate response header X-Flipper-Dup-Resp count="
                                                    + dupCount);
                                    runOnUiThread(
                                            () ->
                                                    ToastUtils.showShort(
                                                            "HTTP "
                                                                    + code
                                                                    + " — Flipper 应见 3 条 X-Flipper-Dup-Resp，OkHttp 解析到 "
                                                                    + dupCount
                                                                    + " 条"));
                                } finally {
                                    response.close();
                                }
                            }
                        });
    }

    public void sse(View view) {
        if (sseCall != null && !sseCall.isCanceled()) {
            sseCall.cancel();
            sseCall = null;
            ToastUtils.showShort("已取消 SSE");
            return;
        }
        Request request = new Request.Builder()
                .url(SSE_URL)
                .header("Accept", "text/event-stream")
                .get()
                .build();
        sseCall = sseClient.newCall(request);
        ToastUtils.showShort("连接 SSE…");
        sseCall.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (call.isCanceled()) {
                    return;
                }
                Log.e(TAG_SSE, "sse onFailure", e);
                runOnUiThread(() -> ToastUtils.showLong("SSE 失败: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (!response.isSuccessful()) {
                    String msg = "SSE HTTP " + response.code();
                    Log.w(TAG_SSE, msg);
                    runOnUiThread(() -> ToastUtils.showLong(msg));
                    response.close();
                    return;
                }
                if (response.body() == null) {
                    runOnUiThread(() -> ToastUtils.showShort("SSE 空 body"));
                    response.close();
                    return;
                }
                BufferedSource source = response.body().source();
                String currentEvent = null;
                StringBuilder dataBuf = new StringBuilder();
                try {
                    while (!call.isCanceled()) {
                        String line = source.readUtf8Line();
                        if (line == null) {
                            break;
                        }
                        if (line.isEmpty()) {
                            if (dataBuf.length() > 0) {
                                final String ev = currentEvent;
                                final String data = dataBuf.toString();
                                dataBuf.setLength(0);
                                currentEvent = null;
                                Log.d(TAG_SSE, "event=" + ev + " data=" + data);
                                runOnUiThread(() -> ToastUtils.showShort(
                                        (ev != null ? "[" + ev + "] " : "") + data));
                            }
                            continue;
                        }
                        if (line.startsWith("event:")) {
                            currentEvent = line.substring(6).trim();
                        } else if (line.startsWith("data:")) {
                            if (dataBuf.length() > 0) {
                                dataBuf.append('\n');
                            }
                            dataBuf.append(line.substring(5).trim());
                        }
                    }
                } catch (IOException e) {
                    if (!call.isCanceled()) {
                        Log.e(TAG_SSE, "sse read", e);
                        runOnUiThread(() -> ToastUtils.showLong("SSE 读取结束: " + e.getMessage()));
                    }
                } finally {
                    response.close();
                    sseCall = null;
                    if (!call.isCanceled()) {
                        runOnUiThread(() -> ToastUtils.showShort("SSE 连接已关闭"));
                    }
                }
            }
        });
    }

    public void loadImageWithOkhttpDialog(View view) {
        Request request = new Request.Builder().url(SAMPLE_IMAGE_URL).get().build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> ToastUtils.showLong("图片请求失败: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (!response.isSuccessful()) {
                        runOnUiThread(
                                () ->
                                        ToastUtils.showLong(
                                                "HTTP " + response.code() + " " + response.message()));
                        return;
                    }
                    if (response.body() == null) {
                        runOnUiThread(() -> ToastUtils.showShort("响应无 body"));
                        return;
                    }
                    byte[] data = response.body().bytes();
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bmp == null) {
                        runOnUiThread(() -> ToastUtils.showShort("无法解码为 Bitmap"));
                        return;
                    }
                    final Bitmap bitmap = bmp;
                    runOnUiThread(() -> showImageInDialog(bitmap));
                } catch (IOException e) {
                    runOnUiThread(() -> ToastUtils.showLong("读取失败: " + e.getMessage()));
                } finally {
                    response.close();
                }
            }
        });
    }

    private void showImageInDialog(final Bitmap bitmap) {
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        ScrollView scroll = new ScrollView(this);
        scroll.setPadding(pad, pad, pad, pad);
        final ImageView imageView = new ImageView(this);
        imageView.setImageBitmap(bitmap);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        scroll.addView(
                imageView,
                new ScrollView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        new AlertDialog.Builder(this)
                .setTitle("OkHttp 图片")
                .setView(scroll)
                .setPositiveButton("关闭", null)
                .setOnDismissListener(
                        dialog -> {
                            imageView.setImageBitmap(null);
                            if (!bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                        })
                .show();
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
        LogUtils.w("xxxxx");
        //OkhttpHookForChucker.getChuckerCollector().onError("dd",new RuntimeException("testxxxxxx"));
    }

    public void alertDialog(View view) {
        new AlertDialog.Builder(this)
                .setTitle("title test")
                .setMessage("i am msg..............................")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("cancel",null)
                .setNeutralButton("center", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToastUtils.showShort("central");
                    }
                }).show();

        new ProgressDialog(this).show();
    }

    public void singleChooseDialog(View view) {
        String[] strs = new String[]{"选型1", "选项2","选型1", "选项2","选型1", "选项2","选型1", "选项2"};
        new AlertDialog.Builder(this)
                .setTitle("title test2 title test2 title test2 title test2 title test2")
                .setSingleChoiceItems(strs, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToastUtils.showLong(strs[which]+", "+which);
                    }
                })
               // .setMessage("i am msg..............................")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("cancel",null)
                .setNeutralButton("center", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ToastUtils.showShort("central");
                    }
                }).show();

    }

    public void logaop_warn(View view) {
        LogUtils.w("waring log test");

    }

    public void logaop_error(View view) {
        LogUtils.e("error log test");
    }

    public void logaop_throwable(View view) {
        LogUtils.w(new RuntimeException("test warn log"));
    }

    public void openNewActivity(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
}