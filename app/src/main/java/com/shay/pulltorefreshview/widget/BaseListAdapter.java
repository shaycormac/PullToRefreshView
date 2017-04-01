package com.shay.pulltorefreshview.widget;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-04-01 10:13 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：封装adapter。
 */
public class BaseListAdapter<T,H extends BaseListAdapter.ViewHolder> extends BaseAdapter
{
    private final List<T> mData;
    private final ViewCreator<T, H> mViewCreator;

    //构造方法
    public BaseListAdapter(List<T> mData, ViewCreator<T, H> mViewCreator) {
        this.mData = mData==null?new ArrayList<T>():mData;
        this.mViewCreator = mViewCreator;
    }

    public BaseListAdapter(ViewCreator<T, H> mViewCreator)
    {
        this(new ArrayList<T>(), mViewCreator);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        final H holder;
        if (convertView==null)
        {
            holder = mViewCreator.createHolder(position, parent);
            convertView = holder.itemView;
        }else 
        {
            holder = (H) convertView.getTag();
        }
        //绑定数据
        mViewCreator.bindData(position,holder, (T) getItem(position));
        return convertView;
    }
    //更新数据
    public void update(List<T> data)
    {
        mData.clear();
        addData(data);
        
    }
    //上拉加载更多数据
    public void addData(List<T> data)
    {
        if (data!=null)
        {

            mData.addAll(data);
            notifyDataSetChanged();
        }
        
    }

    //模仿RecycleView，建立内部封装子类
    public static abstract class ViewHolder
    {
        public final View itemView;
        public ViewHolder(View itemView) {
            this.itemView = itemView;
            itemView.setTag(this);
        }
    }
}
