package com.shay.pulltorefreshview.net;

import android.content.Context;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-03-31 16:52 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：创建这个类的目的，意义。
 */
public class Api
{
    private Context context;
    private CallBack callBack;

    public Api(Context context, CallBack callBack) 
    {
        this.context = context;
        this.callBack = callBack;
    }
    
    
    public void getNum() {
        callBack.onResponse("呵呵哒", false, "错i无");
    }
}
