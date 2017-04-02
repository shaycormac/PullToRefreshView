package com.shay.pulltorefreshview.widget;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.shay.base.PullToRefreshBase;
import com.shay.base.PullToRefreshListView;
import com.shay.base.extras.SoundPullEventListener;
import com.shay.pulltorefreshview.R;
import com.shay.pulltorefreshview.net.CallBack;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-03-31 14:15 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：创建这个类的目的，意义。
 */
public abstract class BaseListView<E> 
{
    public PullToRefreshListView ptrListView;
    public final ArrayList<E> mListItems = new ArrayList<>();
    public Context mContext;
    protected BaseAdapter adapter;
    protected boolean isNoMore;
    //页面控制变量
    protected int page = 1;
    protected int mPerPage = 10;
    //几个状态
    @ActionType
    public int actionType = IDLE;
    public static final int IDLE = 0;
    public static final int INIT = 1;
    public static final int REFRESH = 2;
    public static final int GETMORE = 3;

    @IntDef({IDLE, INIT, REFRESH, GETMORE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType {
    }

    private View view = null;
    protected LinearLayout rlNoCache;
    protected TextView tvNoMoreData;
    protected View headerView = null;

    private View noDataView;
    private FrameLayout rlNoData;

    /**
     * 布局加载管理器
     */
    private LayoutInflater layoutInflater;

    //三个抽象方法等待具体子类实现

    /**
     * 具体的每一个Item
     *
     * @param layoutInflater
     * @param convertView
     * @param e
     * @return
     */
    public abstract View newItemView(LayoutInflater layoutInflater, View convertView, E e);

    /**
     * 网络请求后返回的数据
     *
     * @param response
     * @param actionType
     */
    public abstract void handleResponse(String response, @ActionType int actionType);

    /**
     * 具体的网络请求
     */
    public abstract void asyncData();
    //具体的几个手势状态

    /**
     * 初始化，每一个继承该类的方法构造函数中必须调用这个方法
     */
    public void initListViewStart() {
        if (actionType != IDLE)
            return;
        actionType = INIT;
        page = 1;
        asyncData();
    }

    public void refreshListViewStart() {
        if (actionType != IDLE)
            return;
        actionType = REFRESH;
        page = 1;
        asyncData();
    }

    public void getMoreListViewStart() {
        if (actionType != IDLE)
            return;
        actionType = GETMORE;
        page++;
        asyncData();
    }

    public BaseListView(PullToRefreshListView ptrListView, Context mContext) {
        this.ptrListView = ptrListView;
        this.mContext = mContext;
        layoutInflater = LayoutInflater.from(mContext);
        ensureUi();
    }


    public BaseListView(PullToRefreshListView ptrListView, Context mContext, View headerView) {
        this.ptrListView = ptrListView;
        this.mContext = mContext;
        this.headerView = headerView;
        layoutInflater = LayoutInflater.from(mContext);
        ensureUi();
    }

    /**
     * 确定方法
     */
    private void ensureUi() {
        if (null != headerView)
            ptrListView.getRefreshableView().addHeaderView(headerView);
        adapter = new ListAdapter(layoutInflater);
        //todo 设置没有数据的监听
        view = LayoutInflater.from(mContext).inflate(R.layout.list_no_data_cache, null);
        rlNoCache = (LinearLayout) view.findViewById(R.id.rlNoCache);
        tvNoMoreData = (TextView) view.findViewById(R.id.tvNoMoreData);
        //设置点击事件
        if (null!=rlNoCache)
        {
            rlNoCache.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //监听是否有网络，如果没有，什么也不做，如果有，点击重新加载
                  /*  if (!Utils.isNetworkAvailable(context))
                    {
                        ToastUtil.ToastBottomMsg(context,"网络连接失败，请检查网络设置。");
                    }else
                    {

                        if (ptrListView!=null && view!=null)
                        {
                            ptrListView.getRefreshableView().removeFooterView(view);
                            if (adapter!=null)
                                adapter.notifyDataSetChanged();
                        }
                        //有网了，继续上拉加载
                        getMoreListViewStart();
                    }*/


                    if (ptrListView!=null && view!=null)
                    {
                        ptrListView.getRefreshableView().removeFooterView(view);
                        if (adapter!=null)
                            adapter.notifyDataSetChanged();
                    }
                    //有网了，继续上拉加载
                    getMoreListViewStart();
                }
            });

        }
        ptrListView.setMode(PullToRefreshBase.Mode.BOTH);
        ptrListView.setAdapter(adapter);
        ptrListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                //刷新
                refreshListViewStart();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                //上拉加载更多
                getMoreListViewStart();
            }
        });
       //播放音效
        SoundPullEventListener<ListView> soundListener = new SoundPullEventListener<ListView>(mContext);
        soundListener.addSoundEvent(PullToRefreshBase.State.PULL_TO_REFRESH, R.raw.pull_event);
        soundListener.addSoundEvent(PullToRefreshBase.State.RESET, R.raw.reset_sound);
        soundListener.addSoundEvent(PullToRefreshBase.State.REFRESHING, R.raw.refreshing_sound);
       ptrListView.setOnPullEventListener(soundListener);
        //是否显示EmptyView
        if (ptrListView.getMode() == PullToRefreshBase.Mode.BOTH || ptrListView.getMode() == PullToRefreshBase.Mode.PULL_FROM_START) {
            if (ptrListView.getRefreshableView().getHeaderViewsCount() <= 1) {
                if (noDataView == null) {
                    noDataView = layoutInflater.inflate(R.layout.list_no_data_layout, null);
                    rlNoData = (FrameLayout) noDataView.findViewById(R.id.rl_no_data);
                    rlNoData.setVisibility(View.GONE);
                }
                if (ptrListView != null) {
                    ((ViewGroup) ptrListView.getRefreshableView().getParent()).removeViewInLayout(noDataView);
                    ((ViewGroup) ptrListView.getRefreshableView().getParent()).addView(noDataView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }

                ptrListView.getRefreshableView().setEmptyView(noDataView);
            }
        }


    }

    //自定义Adapter
    class ListAdapter extends BaseAdapter {
        private LayoutInflater layoutInflater;

        public ListAdapter(LayoutInflater layoutInflater) {
            this.layoutInflater = layoutInflater;
        }

        @Override
        public int getCount() {
            return mListItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mListItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return newItemView(layoutInflater, convertView, mListItems.get(position));
        }
    }

    /**
     * 数据回调
     */
    protected CallBack callback = new CallBack() {
        @Override
        public void onResponse(@NonNull String response, @NonNull boolean isCache, String failureMessage) {
            super.onResponse(response, isCache, failureMessage);
            if (GETMORE != actionType)
                mListItems.clear();

            //将数据返回给实现的类
            handleResponse(response, actionType);
            //从数据库中取缓存，并且是上拉，数据库中没有数据了这种情况下，重新赋值
            if (isCache && (actionType == GETMORE && TextUtils.isEmpty(response))) {
                //上拉加载时，请求失败，把page减一，防止丢失数据（每请求一次page在增加）
                if (actionType == GETMORE && page > 1)
                    page = page - 1;
                isNoMore = true;
            }
            if (adapter != null) {
                if (INIT == actionType)
                    adapter.notifyDataSetInvalidated();
                else
                    adapter.notifyDataSetChanged();
            }
/*设置无数据提示图片是否显示*/
            if (rlNoData != null) {
                if (mListItems.size() > 0)
                    rlNoData.setVisibility(View.GONE);
                else
                    rlNoData.setVisibility(View.VISIBLE);
            }
            
            switch(actionType)
            {
              case INIT:
                  break;
              case REFRESH :
                case GETMORE:
                    ptrListView.onRefreshComplete(); 
                  break;
            }
            //恢复到原始状态
            actionType = IDLE;
            //是否显示底部
            controlMoreFooter(isCache);
        }
    };

    private  void controlMoreFooter(boolean isCache)
    {
        if (ptrListView.isRefreshing())
            ptrListView.onRefreshComplete();
        //去掉缓存没有的时候，文件
        ptrListView.getRefreshableView().removeFooterView(view);
        //没有数据了
        if (isNoMore)
        {
            if (isCache)
            {
                if ( tvNoMoreData!=null)
                    tvNoMoreData.setText("别扯了，点我试试~");
                if (rlNoCache!=null)
                    rlNoCache.setClickable(true);
            }else
            {
                if ( tvNoMoreData!=null)
                    tvNoMoreData.setText("我是有底线的~");
                if (rlNoCache!=null)
                    rlNoCache.setClickable(false);
            }
            if (headerView==null)
            ptrListView.getRefreshableView().addFooterView(view);
            ptrListView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            
        }else 
            ptrListView.setMode(PullToRefreshBase.Mode.BOTH);
        
    }


}
