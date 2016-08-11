
final CacheControl.Builder builder = new CacheControl.Builder();
     builder.noCache();//不使用缓存，全部走网络
     builder.noStore();//不使用缓存，也不存储缓存
     builder.onlyIfCached();//只使用缓存
     builder.noTransform();//禁止转码
     builder.maxAge(10, TimeUnit.MILLISECONDS);//指示客户机可以接收生存期不大于指定时间的响应。
     builder.maxStale(10, TimeUnit.SECONDS);//指示客户机可以接收超出超时期间的响应消息
     builder.minFresh(10, TimeUnit.SECONDS);//指示客户机可以接收响应时间小于当前时间加上指定时间的响应。
     CacheControl cache = builder.build();//cacheControl

     CacheControl.FORCE_CACHE; //仅仅使用缓存
     CacheControl.FORCE_NETWORK;// 仅仅使用网络


     new CacheControl.Builder() .maxAge(0, TimeUnit.SECONDS)//这个是控制缓存的最大生命时间
     new CacheControl.Builder().maxStale(365, TimeUnit.DAYS)//这个是控制缓存的过时时间
     这个跟上面的控制缓存的时间有什么区别？
     发现如果.maxAge(0, TimeUnit.SECONDS)设置的时间比拦截器长是不起效果，
     如果设置比拦截器设置的时间短就会以这个时间为主，我觉得是为了方便控制。
     .maxStale(365, TimeUnit.DAYS)设置的是过时时间，我觉得okthhp缓存分成了两个来考虑，
     一个是为了请求时直接拿缓存省流量，一个是为了下次进入应用时可以直接拿缓存。


     Request request = new Request.Builder()
          .url("https://api.github.com/repos/square/okhttp/issues")
          .header("User-Agent", "OkHttp Headers.java")
          .addHeader("Accept", "application/json; q=0.5")
          .addHeader("Accept", "application/vnd.github.v3+json")
          .build();



   如果返回的数据量比较大，就不要直接用response.body().string()，应该用 response.body().charStream()