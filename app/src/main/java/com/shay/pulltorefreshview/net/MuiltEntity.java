package com.shay.pulltorefreshview.net;

import android.support.annotation.IntDef;

import com.shay.pulltorefreshview.entity.One;
import com.shay.pulltorefreshview.entity.Three;
import com.shay.pulltorefreshview.entity.Two;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-04-01 15:09 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：listView的多布局类型。
 */
public class MuiltEntity
{
    public static final int ONE = 0x00000001;
    public static final int TWO = 0x00000002;
    public static final int THREE = 0x00000003;
    public static final int FOUR = 0x00000004;
   @MuiltType
    public int type;
    
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ONE,TWO,THREE,FOUR})
    public @interface MuiltType
    {
        
    }

    public One one;
    public Two two;
    public Three three;
}
