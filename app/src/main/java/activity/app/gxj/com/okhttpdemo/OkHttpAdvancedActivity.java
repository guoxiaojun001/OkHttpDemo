package activity.app.gxj.com.okhttpdemo;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okio.BufferedSink;

public class OkHttpAdvancedActivity extends AppCompatActivity {

    String TAG = "OkHttpAdvancedActivity";
    String url = "https://raw.github.com/square/okhttp/master/README.md";
    private final OkHttpClient okHttpClient = new OkHttpClient();

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
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);

        result = (TextView) findViewById(R.id.result);
    }

    //get请求
    public void testGet(View view) {
        result.setText("正在请求。。。。");
        new Thread(){
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url(url)
                        .build();
                Response response = null;
                try {
                    response = okHttpClient.newCall(request) .execute();

                    if (response.isSuccessful()) {
                        data = response.body().string();
                        handler.sendMessage(handler.obtainMessage(0,data));
                    }else{
                        handler.sendMessage(handler.obtainMessage(0,"get响应失败"));
                    }
                    System.out.println(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void testAsyncGet(View view) {
        result.setText("正在请求。。。。");
        final Request request = new Request.Builder().url(
                "http://publicobject.com/helloworld.txt").build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendMessage(handler.obtainMessage(0,"请求失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("call = [" + call + "], response = [" + response + "]");
                if (response.isSuccessful()) {
                    data = response.body().string();
                    handler.sendMessage(handler.obtainMessage(0,data));

                }else {
                    handler.sendMessage(handler.obtainMessage(0,"异步get响应失败"));
                }

                Headers headers = response.headers();
                for (int i = 0; i < headers.size(); i++) {
                    System.out.println(headers.name(i) + ": " + headers.value(i));
                }

                System.out.println(data);
            }
        });
    }

    //post请求
    public void testPost(View view){
        result.setText("正在请求。。。。");
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final RequestBody body = RequestBody.create(JSON, json2String("Jesse", "Jake"));

        new Thread(){
            @Override
            public void run() {
                Request request = new Request.Builder()
                        .url("http://www.roundsapp.com/post")
                        .post(body)
                        .build();
                Response response = null;

                try {
                    response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful()) {
                        data = response.body().string();
                        handler.sendMessage(handler.obtainMessage(0,data));
                    }else{
                        handler.sendMessage(handler.obtainMessage(0,"响应失败"));
                    }

                    System.out.println(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    //post 异步请求
    public void testAsyncPost(View view){
        result.setText("正在请求。。。。");
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        final RequestBody body = RequestBody.create(JSON, json2String("Jesse", "Jake"));
        Request request = new Request.Builder()
                .url("http://www.roundsapp.com/post")
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendMessage(handler.obtainMessage(0,"失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (response.isSuccessful()) {
                    data = response.body().string();
                    handler.sendMessage(handler.obtainMessage(0,data));
                }else{
                    handler.sendMessage(handler.obtainMessage(0,"响应失败"));
                }
            }
        });
    }

    String json2String(String player1, String player2) {
        return "{'winCondition':'HIGH_SCORE',"
                + "'name':'Bowling',"
                + "'round':4,"
                + "'lastSaved':1367702411696,"
                + "'dateStarted':1367702378785,"
                + "'players':["
                + "{'name':'" + player1 + "','history':[10,8,6,7,8],'color':-13388315,'total':39},"
                + "{'name':'" + player2 + "','history':[6,10,5,10,10],'color':-48060,'total':41}"
                + "]}";
    }

    public void testHeader(View view){
        result.setText("正在请求。。。。");
        final Request request = new Request.Builder()
                .url("https://api.github.com/repos/square/okhttp/issues")
                // User-Agent   User-Agent的内容包含发出请求的用户信息    User-Agent: Mozilla/5.0 (Linux; X11)
                .header("User-Agent", "OkHttp Headers.java")
                // Accept   指定客户端能够接收的内容类型  Accept: text/plain, text/html
                .addHeader("Accept", "application/json; q=0.5")
                .addHeader("Accept", "application/vnd.github.v3+json")
                .build();

        new Thread(){
            @Override
            public void run() {
                Response response = null;
                try {
                    response = okHttpClient.newCall(request).execute();

                    if (response.isSuccessful()) {
                        System.out.println("Server: " + response.header("Server"));
                        System.out.println("Date: " + response.header("Date"));
                        System.out.println("Vary: " + response.headers("Vary"));

                        data = response.body().string();
                        handler.sendMessage(handler.obtainMessage(0,data));
                    }else {
                        handler.sendMessage(handler.obtainMessage(0,"请求失败"));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    //添加授权证书
    public void testAuthenticate(View view){
        result.setText("正在请求。。。。");
        OkHttpClient  okHttpClient = new OkHttpClient.Builder()
                // 授权证书
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        System.out.println("Authenticating for response: " + response);
                        System.out.println("Challenges: " + response.challenges());
                        String credential = Credentials.basic("jesse", "password1");
                        // HTTP授权的授权证书  Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
                        return response
                                .request()
                                .newBuilder()
                                .header("Authorization", credential)
                                .build();
                    }
                })
                .build();

        Request request = new Request.Builder().url(
                "http://publicobject.com/secrets/hellosecret.txt").build();

        Response response = null;

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendMessage(handler.obtainMessage(0,"请求失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    data = response.body().string();
                    handler.sendMessage(handler.obtainMessage(0,data));
                }else{
                    handler.sendMessage(handler.obtainMessage(0,"响应失败"));
                }
            }
        });

    }

    //取消任务
    public void testCancelCall(View view) {
        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/2")
                .build();

        final long startNanos = System.nanoTime();
        final Call call = okHttpClient.newCall(request);

        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        executor.schedule(new Runnable() {
            @Override
            public void run() {
                System.out.printf("%.2f Canceling call.%n", (System.nanoTime() - startNanos) / 1e9f);
                call.cancel();
                System.out.printf("%.2f Canceled call.%n", (System.nanoTime() - startNanos) / 1e9f);
            }
        }, 1, TimeUnit.SECONDS);

        System.out.printf("%.2f Executing call.%n", (System.nanoTime() - startNanos) / 1e9f);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.printf("%.2f Call failed as expected: %s%n",
                        (System.nanoTime() - startNanos) / 1e9f, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.printf("%.2f Call was expected to fail, but completed: %s%n",
                        (System.nanoTime() - startNanos) / 1e9f, response);
            }
        });
        //Response response = call.execute();
    }


    public void testCertificatePinning(View view) throws IOException {
        OkHttpClient  yOkHttpClient = new OkHttpClient.Builder()
                .certificatePinner(new CertificatePinner.Builder()
                        .add("publicobject.com", "sha1/DmxUShsZuNiqPQsX2Oi9uv2sCnw=")
                        .add("publicobject.com", "sha1/SXxoaOSEzPC6BgGmxAt/EAcsajw=")
                        .add("publicobject.com", "sha1/blhOM3W9V/bVQhsWAcLYwPU6n24=")
                        .add("publicobject.com", "sha1/T5x9IXmcrQ7YuQxXnxoCmeeQ84c=")
                        .build())
                .build();

        Request request = new Request.Builder().url("https://publicobject.com/robots.txt").build();


        yOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"----请求失败---");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    data = response.body().string();
                    handler.sendMessage(handler.obtainMessage(0,data));

                    for (Certificate certificate : response.handshake().peerCertificates()) {
                        Log.d(TAG,"certificate = " + CertificatePinner.pin(certificate));
                        System.out.println(CertificatePinner.pin(certificate));
                    }
                }else {
                    Log.d(TAG,"----返回失败---");
                }
            }
        });
    }


    public void testTimeOut(View view){
        result.setText("正在加载....");
        OkHttpClient yOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/2") // This URL is served with a 2 second delay.
                .build();

        yOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handler.sendMessage(handler.obtainMessage(0, "请求失败"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    data = response.body().string();
                    handler.sendMessage(handler.obtainMessage(0, data));
                }else {
                    handler.sendMessage(handler.obtainMessage(0, "响应失败"));
                }

                System.out.println("Response completed: " + response);
            }
        });
    }


    public void testLogInterceptor(View view) throws IOException {
        result.setText("正在加载....");
        OkHttpClient yOkHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggingInerceptor())
                .build();

        Request request = new Request.Builder()
                .url("https://publicobject.com/helloworld.txt")
                .build();

        yOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"----返回失败---");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                data = response.body().string();
                Log.d(TAG,"data = " + data);

            }
        });

    }

    private static class LoggingInerceptor implements Interceptor {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
            long t1 = System.nanoTime();
            Request request = chain.request();
            Log.d("TAG","----------start---------------" );
            System.out.println(String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            System.out.println(String.format("Received response for %s in %.1fms%n%s",
                    request.url(), (t2 - t1) / 1e6d, response.headers()));
            Log.d("TAG","----------end-----------------" );
            return response;
        }
    }

    public void testPerCallSettings(View view) {
        result.setText("正在加载....");
        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/1") // This URL is served with a 1 second delay.
                .build();
        OkHttpClient copy1 = okHttpClient.newBuilder()
                .readTimeout(500, TimeUnit.MILLISECONDS)
                .build();

        copy1.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Response 1 failed: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("Response 1 succeeded: " + response);
            }
        });

        // Copy to customize OkHttp for this request.
        OkHttpClient copy2 = okHttpClient.newBuilder()
                .readTimeout(3000, TimeUnit.MILLISECONDS)
                .build();

        copy2.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Response 2 failed: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                System.out.println("Response 2 succeeded: " + response);
            }
        });
    }

    public void testPostFile(View view)  {
        MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

        File file = new File(Environment.getExternalStorageDirectory(),"channel.txt");

        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, file))
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("失败 ！" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                data = response.body().string();
                Log.d(TAG,"data = " + data);
                handler.sendMessage(handler.obtainMessage(0, data));
            }
        });
    }

    public void testPostForm(View view) throws IOException {
        FormBody formBody = new FormBody.Builder()
                .add("search", "nba")
                .build();

        Request request = new Request.Builder()
                .url("https://en.wikipedia.org/w/index.php")
                .post(formBody)
                .build();


        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("失败 ！" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                data = response.body().string();
                Log.d(TAG,"data = " + data);
                handler.sendMessage(handler.obtainMessage(0, data));
            }
        });
    }


    public void testPostMultipart(View view) {
        String IMGUR_CLIENT_ID = "9199fdef135c122";
        MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("title", "Square Logo")
                .addFormDataPart("image", "logo-square.png",
                        RequestBody.create(MEDIA_TYPE_PNG,
                                new File("website/static/logo-square.png")))
                .build();

        Request request = new Request.Builder()
                .header("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                .url("https://api.imgur.com/3/image")
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"onFailure = " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                data = response.body().string();
                Log.d(TAG,"data = " + data);
                handler.sendMessage(handler.obtainMessage(0, data));
            }
        });
    }

    public void testPostStreaming(View view) {
        final MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");

        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return MEDIA_TYPE_MARKDOWN;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("Numbers\n");
                sink.writeUtf8("-------\n");
                for (int i = 2; i <= 997; i++) {
                    sink.writeUtf8(String.format(" * %s = %s\n", i, factor(i)));
                }
            }

            private String factor(int n) {
                for (int i = 2; i < n; i++) {
                    int x = n / i;
                    if (x * i == n) return factor(x) + " × " + i;
                }
                return Integer.toString(n);
            }
        };

        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"onFailure = " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                data = response.body().string();
                Log.d(TAG,"data = " + data);
                handler.sendMessage(handler.obtainMessage(0, data));
            }
        });
    }

    public void testPostString(View view) throws IOException {
        MediaType MEDIA_TYPE_MARKDOWN = MediaType.parse("text/x-markdown; charset=utf-8");
        String postBody = ""
                + "Releases\n"
                + "--------\n"
                + "\n"
                + " * _1.0_ May 6, 2013\n"
                + " * _1.1_ June 15, 2013\n"
                + " * _1.2_ August 11, 2013\n";
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(MEDIA_TYPE_MARKDOWN, postBody))
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG,"onFailure = " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                data = response.body().string();
                Log.d(TAG,"data = " + data);
                handler.sendMessage(handler.obtainMessage(0, data));
            }
        });
    }

}
