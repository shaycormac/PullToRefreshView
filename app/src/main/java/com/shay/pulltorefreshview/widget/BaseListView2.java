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
import com.shay.pulltorefreshview.widget.adapter.ItemViewDelegate;
import com.shay.pulltorefreshview.widget.adapter.MultiItemTypeAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-04-01 14:50 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：多类型item的listView。
 */
public abstract class BaseListView2<E> 
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
    @ActionType2
    public int actionType = IDLE;
    public static final int IDLE = 0;
    public static final int INIT = 1;
    public static final int REFRESH = 2;
    public static final int GETMORE = 3;

    @IntDef({IDLE, INIT, REFRESH, GETMORE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionType2 {
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

    //两个抽象方法等待具体子类实现
    
    /**
     * 网络请求后返回的数据
     *
     * @param response
     * @param actionType
     */
    public abstract void handleResponse(String response, @ActionType2 int actionType);

    /**
     * 具体的网络请求
     */
    public abstract void asyncData();

    /**
     * 多种类型的itemType，必须返回一个相应的类型集合。
     */
    public abstract List<ItemViewDelegate<E>> getItemViewDelegates();
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

    public BaseListView2(PullToRefreshListView ptrListView, Context mContext) {
        this.ptrListView = ptrListView;
        this.mContext = mContext;
        layoutInflater = LayoutInflater.from(mContext);
        ensureUi();
    }


    public BaseListView2(PullToRefreshListView ptrListView, Context mContext, View headerView) {
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
       // adapter = new ListAdapter(layoutInflater);
        adapter = new MuiltAdapter(mContext, mListItems);
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
        ptrListView.setMode(PullToRefreshBase.BOTH);
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
        soundListener.addSoundEvent(PullToRefreshBase.PULL_TO_REFRESH, R.raw.pull_event);
        soundListener.addSoundEvent(PullToRefreshBase.RESET, R.raw.reset_sound);
        soundListener.addSoundEvent(PullToRefreshBase.REFRESHING, R.raw.refreshing_sound);
        ptrListView.setOnPullEventListener(soundListener);
        //是否显示EmptyView
        if (ptrListView.getMode() == PullToRefreshBase.BOTH || ptrListView.getMode() == PullToRefreshBase.PULL_FROM_START) {
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
    
    //继承的adapter
    class MuiltAdapter extends MultiItemTypeAdapter<E>
    {
        List<ItemViewDelegate<E>> itemViewDelegates;

        public MuiltAdapter(Context context, List<E> mDatas) 
        {
            super(context, mDatas,layoutInflater);
            //添加不同种的类型
            itemViewDelegates = getItemViewDelegates();
            if (itemViewDelegates==null || itemViewDelegates.isEmpty())
                throw new IllegalArgumentException("至少添加一种item类型");
            int size = itemViewDelegates.size();
            for (int i = 0; i < size; i++) 
            {
                addItemViewDelegate(itemViewDelegates.get(i));   
            }
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
            if ( rlNoData != null) {
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
            //只有上拉更多的时候才显示没有了
           // if (actionType==GETMORE)
            ptrListView.getRefreshableView().addFooterView(view);
            ptrListView.setMode(PullToRefreshBase.PULL_FROM_START);

        }else
            ptrListView.setMode(PullToRefreshBase.BOTH);

    }

}
