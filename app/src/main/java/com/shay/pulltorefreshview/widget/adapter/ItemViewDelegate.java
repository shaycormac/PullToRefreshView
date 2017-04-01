package com.shay.pulltorefreshview.widget.adapter;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-04-01 14:11 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：每一个itemView的代理。
 */
public interface ItemViewDelegate<T>
{
    /**
     * 视图的资源文件
     * @return
     */
     int getItemViewLayoutId();

    /**
     * 是否当前对应的位置的布局，是返回true,不是，返回false
     * @param item
     * @param position
     * @return
     */
     boolean isForViewType(T item, int position);

    /**
     * 每个位置上对应的视图，逻辑主要在这里写。
     * @param holder
     * @param t
     * @param position
     */
     void convert(ViewHolder holder, T t, int position);
}
