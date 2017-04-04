package com.shay.pulltorefreshview.net;

import android.content.Context;
import android.os.Handler;
import android.support.v4.util.ArrayMap;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017/4/3 21:28
 * @email 邮箱： 574583006@qq.com
 * @content 说明：OkHttp的封装类。
 */
public class OkHttpInstance 
{
    public static final String HOST = "121.41.38.32:3000";
    public static final String PROTOCOL_HOST = "http://" + HOST;
    private OkHttpClient okHttpClient;
    private OkHttpClient.Builder builder;
    private Handler handler;
    public static class Holder
    {
        private static OkHttpInstance instance = new OkHttpInstance();
    }

   

    private OkHttpInstance() 
    {
     
         builder = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS);
        handler = new Handler();
    }
    //一定要在得到对象后就调用
    public OkHttpInstance setCache(Context context)
    {
        File sdCache = context.getExternalCacheDir();
        int cacheSize = 10 * 1024 * 1024;
        builder.cache(new Cache(sdCache.getAbsoluteFile(), cacheSize));
        okHttpClient = builder.build();
        return getInstance();
    }
    public static OkHttpInstance getInstance()
    {
        return Holder.instance;
    }
    
    public OkHttpClient getOkHttpClient()
    {
        return okHttpClient;
    }
    
    public void post(Context context, String path, ArrayMap<String, String> params, Callback asynHandler)
    {
        //遍历参数
        FormBody.Builder builder = new FormBody.Builder();
        int size = params.size();
        for (int i = 0; i < size; i++) 
        {
            builder.add(params.keyAt(i), params.valueAt(i));    
        }
        FormBody formBody = builder.build();
        String url = PROTOCOL_HOST + path;
        Request request = new Request.Builder().url(url).post(formBody).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(asynHandler);
    }
}
