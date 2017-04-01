package com.shay.pulltorefreshview.widget;

import android.view.ViewGroup;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-04-01 10:10 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：该接口用于ListView创建ViewHolder以及绑定数据。
 */
public interface ViewCreator<T,H extends BaseListAdapter.ViewHolder>
{
    /**
     * 不同位置生成相应的view
     * @param position
     * @param parent
     * @return
     */
    H createHolder(int position, ViewGroup parent);

    /**
     * 设置每个item的视图内容
     * @param position 在列表的位置
     * @param holder 该位置对应的视图
     * @param data
     */
    void bindData(int position, H holder, T data);
}
