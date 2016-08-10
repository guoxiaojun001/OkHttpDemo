package activity.app.gxj.com.okhttpdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by gxj on 2016/8/9.
 */
public class CacheActivity extends AppCompatActivity {

    private final static String TAG = "CacheActivity1";
    String url = "http://publicobject.com/helloworld.txt";

    TextView result;
    Button buttonPanel;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String data = msg.obj.toString();
            Log.d(TAG, "handleMessage" + data);
            switch (msg.what){
                case 0:
                    result.setText("");
                    Toast.makeText(CacheActivity.this,data,Toast.LENGTH_SHORT).show();
                    result.setText(data);
                    break;

                case 1:
                    break;
            }
        }
    };

    //修改系统原有的 设置时间
    CacheControl FORCE_CACHE_MY = new CacheControl.Builder()
            .onlyIfCached()
            .maxStale(60, TimeUnit.SECONDS)
            .build();

    /***
     * 拦截器，保存缓存的方法
     * 2016年7月29日11:22:47
     */
    Interceptor interceptor = new Interceptor() {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            if (isNetworkAvailable()) {
                Response response = chain.proceed(request);
                int maxAge = 60; // 在线缓存在1分钟内可读取
                String cacheControl = request.cacheControl().toString();
                Log.d(TAG, "在线缓存在1分钟内可读取" + cacheControl);
                return response.newBuilder()
                        .removeHeader("Pragma")
                        .removeHeader("Cache-Control")
                        .header("Cache-Control", "public, max-age=" + maxAge)
                        .build();
            } else {
                Log.d(TAG, "离线时缓存时间设置");
                request = request.newBuilder()
                        //.cacheControl(FORCE_CACHE_MY)//此处设置了7秒---修改了系统方法
                        .cacheControl(CacheControl.FORCE_CACHE)//或者直接用系统的
                        .build();

                Response response = chain.proceed(request);
                //下面注释的部分设置也没有效果，因为在上面已经设置了
                return response.newBuilder()
//                        .removeHeader("Pragma")
//                        .removeHeader("Cache-Control")
//                        .header("Cache-Control", "public, only-if-cached, max-stale=50")
                        .build();
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache);
        result = (TextView) findViewById(R.id.result);
        buttonPanel = (Button) findViewById(R.id.buttonPanel);

        buttonPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initGet();
            }
        });
    }

    /***
     * 获取服务器数据
     */
    private void initGet() {
        //设置缓存
        File httpCacheDirectory = new File(CacheActivity.this.getCacheDir(), "okthhpqq");
        Cache cache = null;
        try {
            cache = new Cache(httpCacheDirectory, 10 * 1024 * 1024);
        } catch (Exception e) {
            Log.e("OKHttp", "Could not create http cache", e);
        }
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(cache)
                .addInterceptor(interceptor)
                .addNetworkInterceptor(interceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendMessage(handler.obtainMessage(0,"数据请求失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String data = response.body().string();
                Log.i(TAG, "data == " + data);
                if (response.isSuccessful()) {
                    handler.sendMessage(handler.obtainMessage(0,data));
                } else {
                    handler.sendMessage(handler.obtainMessage(0,"数据请求失败"));
                }
            }
        });

    }

//    public void cancelTask() throws Exception {
//        Request request = new Request.Builder()
//                .url("http://httpbin.org/delay/2") // This URL is served with a 2 second delay.
//                .build();
//
//        final long startNanos = System.nanoTime();
//        final Call call = client.newCall(request);
//
//        // Schedule a job to cancel the call in 1 second.
//        executor.schedule(new Runnable() {
//            @Override
//            public void run() {
//                System.out.printf("%.2f Canceling call.%n", (System.nanoTime() - startNanos) / 1e9f);
//                call.cancel();
//                System.out.printf("%.2f Canceled call.%n", (System.nanoTime() - startNanos) / 1e9f);
//            }
//        }, 1, TimeUnit.SECONDS);
//
//        try {
//            System.out.printf("%.2f Executing call.%n", (System.nanoTime() - startNanos) / 1e9f);
//            Response response = call.execute();
//            System.out.printf("call is cancel:" + call.isCanceled() + "%n");
//            System.out.printf("%.2f Call was expected to fail, but completed: %s%n",
//                    (System.nanoTime() - startNanos) / 1e9f, response);
//        } catch (IOException e) {
//            System.out.printf("%.2f Call failed as expected: %s%n",
//                    (System.nanoTime() - startNanos) / 1e9f, e);
//        }
//    }

    //网络判断
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

}
