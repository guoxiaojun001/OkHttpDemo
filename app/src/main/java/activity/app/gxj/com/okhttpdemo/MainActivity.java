package activity.app.gxj.com.okhttpdemo;

import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String url = "http://publicobject.com/helloworld.txt";
    private final OkHttpClient client = new OkHttpClient();

    Button cache, more;
    TextView result;
    String data;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0:
                    result.setText(msg.obj.toString());
                    break;

                case 1:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = (TextView) findViewById(R.id.result);
        cache = (Button) findViewById(R.id.cache);
        more = (Button) findViewById(R.id.more);

        cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CacheActivity.class));
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,OkHttpAdvancedActivity.class));
            }
        });
    }


    //一般okhttp异步请求 带回调监听
    public void request(View view){
        result.setText("正在请求...");
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("OKHTTPRE","onFailure = " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //NOT UI Thread,刷新ui需要handler发送消息到主线程执行
                if(response.isSuccessful()){
                    data = response.body().string();
                    Log.d("OKHTTPRE","body = " + data);
                    handler.sendMessage(handler.obtainMessage(0,data));
                }
            }
        });

    }

    //直接execute 会在主线程执行，所以需要创建线程来执行
    public void execute(View view) throws IOException {
        result.setText("正在请求...");
        new Thread(){
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = null;

                try {
                    response = client.newCall(request).execute();

                    if(response.isSuccessful()){
                        data = response.body().string();
                        Log.d("OKHTTPRE","body = " + data);

                        handler.sendMessage(handler.obtainMessage(0,data));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void downLoadFile(View view){
        String url = "http://p0.ifengimg.com/a/2016_33/abe707743d66cdc_size27_w570_h692.jpg";
        Request request = new Request.Builder().url(url).build();
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                //把请求成功的response转为字节流
                InputStream inputStream = response.body().byteStream();
                //在这里用到了文件输出流
                FileOutputStream fileOutputStream =
                        new FileOutputStream(new File(
                                Environment.getExternalStorageDirectory(),"/nzt.jpg"));
                //定义一个字节数组
                byte[] buffer = new byte[2048];
                int len = 0;
                while ((len = inputStream.read(buffer)) != -1) {
                    //写出到文件
                    fileOutputStream.write(buffer, 0, len);
                }
                //关闭输出流
                fileOutputStream.flush();
                Log.d("wuyinlei", "文件下载成功...");
            }
        });
    }



    public void postData(View view){
//        result.setText("正在请求...");
//        RequestBody formBody = new FormEncodingBuilder()
//                .add("platform", "android")
//                .add("name", "bug")
//                .add("subject", "XXXXXXXXXXXXXXX")
//                .build();
//
//        final Request request = new Request.Builder()
//                .url(url)
//                .post(formBody)
//                .build();
//
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Response response = client.newCall(request).execute();
//                    if (response.isSuccessful()) {
//                        data = response.body().string();
//                        Log.d("OKHTTPRE","body = " + data);
//                        handler.sendMessage(handler.obtainMessage(0,data));
//                    } else {
//                        throw new IOException("Unexpected code " + response);
//                    }
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }).start();

    }

}
