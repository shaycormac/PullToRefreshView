package com.shay.pulltorefreshview.widget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-04-01 14:16 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：多类型adapter。
 */
public class MultiItemTypeAdapter<T> extends BaseAdapter 
{
    protected Context context;
    protected List<T> mDatas;

    private ItemViewDelegateManager mItemViewDelegateManager;

    private LayoutInflater layoutInflater;

    public MultiItemTypeAdapter(Context context, List<T> mDatas,LayoutInflater layoutInflater) {
        this.context = context;
        this.mDatas = mDatas;
        mItemViewDelegateManager = new ItemViewDelegateManager();
        this.layoutInflater = layoutInflater;
    }

    /**
     * 添加多种布局文件
     * @param itemViewDelegate
     * @return
     */
    public MultiItemTypeAdapter addItemViewDelegate(ItemViewDelegate<T> itemViewDelegate)
    {
        mItemViewDelegateManager.addDelegate(itemViewDelegate);
        return this;
    }

    /**
     * 使用管理器
     * @return
     */
    private boolean useItemViewDelegateManager() {
        return mItemViewDelegateManager.getItemViewDelegateCount() > 0;
    }

    /**
     * baseAdapter的一个方法，得到类型的总数
     * @return
     */
    @Override
    public int getViewTypeCount() {
        if (useItemViewDelegateManager())
            return mItemViewDelegateManager.getItemViewDelegateCount();
        return super.getViewTypeCount();
    }

    @Override
    public int getItemViewType(int position)
    {
        if (useItemViewDelegateManager())
            return mItemViewDelegateManager.getItemViewType(mDatas.get(position), position);
        return super.getItemViewType(position);
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int position) {
        return mDatas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
//重点方法
    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        ItemViewDelegate itemViewDelegate = mItemViewDelegateManager.getItemViewDelegate(mDatas.get(position), position);
        int layoutId = itemViewDelegate.getItemViewLayoutId();
        ViewHolder viewHolder = null;
        if (convertView==null)
        {
            View itemView = layoutInflater.inflate(layoutId, parent, false);
            viewHolder = new ViewHolder(context, itemView, position);
            viewHolder.layoutId = layoutId;
            //todo 对外暴漏的方法
            onViewHolderCreated(viewHolder, viewHolder.getConvertView());
        }else 
        {
            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.position = position;
        }
        convert(viewHolder, (T) getItem(position),position);
        return viewHolder.getConvertView();
    }
    //对外的两个方法
    protected void convert(ViewHolder viewHolder,T item,int position)
    {
        mItemViewDelegateManager.convert(viewHolder, item, position);
    }

    public void onViewHolderCreated(ViewHolder holder , View itemView )
    {}
}
