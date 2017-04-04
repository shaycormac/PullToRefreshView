package com.shay.pulltorefreshview.net;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.shay.base.utils.VolleyLog;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017/4/3 21:59
 * @email 邮箱： 574583006@qq.com
 * @content 说明：创建这个类的意义，目的。
 */
public class BaseApi 
{
    protected CallBack callBack;
    protected Context context;
    private Handler mHandler;

    public BaseApi(CallBack callBack, Context context) {
        this.callBack = callBack;
        this.context = context;
        mHandler = new Handler();
    }
    
    protected Callback asynHandler = new Callback() 
    {
        @Override
        public void onFailure(Call call, IOException e) 
        {
            final String failTureMessage = e.getMessage();
            mHandler.post(new Runnable()
            {
                @Override
                public void run()
                {
                    if (callBack!=null)
                        callBack.onFailure(failTureMessage);
                }
            });
            
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException
        {
            
            final String str;
            final boolean isCache;
            Response response1;
            if (null != response.cacheResponse()) {
                isCache = true;
                response1 = response.cacheResponse();
                Log.i("wangshu", "cache---" + response1.toString());
            } else {
                response1 = response.networkResponse();
                isCache = false;
                Log.i("wangshu", "network---" + response1.toString());
            }
            //得到字符串
            str = response.body().string();
            VolleyLog.d("得到的字符串 %s",str);
        //    str = new String(response1.body().bytes());
            mHandler.post(new Runnable() 
            {
                @Override
                public void run() 
                {
                    if (callBack!=null)
                        callBack.onResponse(str, isCache, null);
                }
            });
            
        }
    };
}
