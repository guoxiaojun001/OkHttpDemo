package activity.app.gxj.com.okhttpdemo;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    String url = "http://publicobject.com/helloworld.txt";
    private final OkHttpClient client = new OkHttpClient();

    Button cache;
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

        //设置cookies 超时时间等
        client.setCookieHandler(
                new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
        client.setConnectTimeout(15000, TimeUnit.SECONDS);
        client.setReadTimeout(15000, TimeUnit.SECONDS);
        client.setWriteTimeout(15000, TimeUnit.SECONDS);

        result = (TextView) findViewById(R.id.result);
        cache = (Button) findViewById(R.id.cache);

        cache.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,CacheActivity.class));
            }
        });
    }


    //一般okhttp异步请求 带回调监听
    public void request(View view){
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                Log.d("OKHTTPRE","onFailure = " + e.getMessage());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                //NOT UI Thread 在子线程执行
                Log.d("OKHTTPRE","code = " + response.code());
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
        new Thread(){
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://publicobject.com/helloworld.txt")
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

    /**
     * Request request = new Request.Builder()
     .url("https://api.github.com/repos/square/okhttp/issues")
     .header("User-Agent", "OkHttp Headers.java")
     .addHeader("Accept", "application/json; q=0.5")
     .addHeader("Accept", "application/vnd.github.v3+json")
     .build();
     */
    public void postData(View view){
        RequestBody formBody = new FormEncodingBuilder()
                .add("platform", "android")
                .add("name", "bug")
                .add("subject", "XXXXXXXXXXXXXXX")
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        data = response.body().string();
                        Log.d("OKHTTPRE","body = " + data);
                        handler.sendMessage(handler.obtainMessage(0,data));
                    } else {
                        throw new IOException("Unexpected code " + response);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

}
