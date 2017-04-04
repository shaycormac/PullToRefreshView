package com.shay.pulltorefreshview.net;

import android.content.Context;
import android.support.v4.util.ArrayMap;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-03-31 16:52 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：创建这个类的目的，意义。
 */
public class Api extends BaseApi
{
    
    public Api(Context context, CallBack callBack) 
    {
        super(callBack,context);
    
    }
    
    
    public void getNum() {
        callBack.onResponse("呵呵哒", false, "错i无");
    }
    
    public void getEventList(String pageSize, String pageNo)
    {
        ArrayMap<String, String> arrayMap = new ArrayMap<>();
        arrayMap.put("pageSize", pageSize);
        arrayMap.put("pageNo", pageNo);
        OkHttpInstance.getInstance().setCache(context).post(context, "/assn/indexVitality", arrayMap, asynHandler);
    }
}
