package com.shay.pulltorefreshview.list;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.shay.base.PullToRefreshListView;
import com.shay.pulltorefreshview.R;
import com.shay.pulltorefreshview.entity.One;
import com.shay.pulltorefreshview.entity.Three;
import com.shay.pulltorefreshview.entity.Two;
import com.shay.pulltorefreshview.net.Api;
import com.shay.pulltorefreshview.net.MuiltEntity;
import com.shay.pulltorefreshview.widget.BaseListView2;
import com.shay.pulltorefreshview.widget.adapter.ItemViewDelegate;
import com.shay.pulltorefreshview.widget.adapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-04-01 15:16 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：多类型的item布局，适合listView。
 */
public class MuiltTypeList extends BaseListView2<MuiltEntity>  
{
    int num;
    public MuiltTypeList(PullToRefreshListView ptrListView, Context mContext) {
        super(ptrListView, mContext);
        initListViewStart();
    }
    public MuiltTypeList(PullToRefreshListView ptrListView, Context mContext, View headView) {
        super(ptrListView, mContext,headView);
        initListViewStart();
    }

    @Override
    public void handleResponse(String response, @ActionType2 int actionType) 
    {
        //加载假数据
        if (num<5)
        {
            //添加数据
            mListItems.addAll(initPerson());
            num++;
            isNoMore = false;
        }else
            isNoMore = true;

    }

    @Override
    public void asyncData() 
    {
        new Api(mContext,callback).getNum();
    }

    //核心！！
    @Override
    public List<ItemViewDelegate<MuiltEntity>> getItemViewDelegates() 
    {
        //添加三个布局文件
        List<ItemViewDelegate<MuiltEntity>> list = new ArrayList<>();
        list.add(new OneDelegate());
        list.add(new TwoDelegate());
        list.add(new ThreeDelegate());
        return list;
    }
    //需要的三个布局文件
    class OneDelegate implements ItemViewDelegate<MuiltEntity>
    {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.muilt_one_layout;
        }

        @Override
        public boolean isForViewType(MuiltEntity item, int position) 
        {
            //判断类型是不是喽
            int type = item.type;
            if (type== MuiltEntity.ONE)
                return true;
            else 
            return false;
        }

        @Override
        public void convert(ViewHolder holder, MuiltEntity muiltEntity, final int position)
        {
            //实际操作！！！
            holder.setText(R.id.tvOne1, muiltEntity.one.name);
            holder.setText(R.id.tvOne2, muiltEntity.one.age + "");
            holder.setOnClickListener(R.id.tvOne2, new View.OnClickListener() {
                @Override
                public void onClick(View v) 
                {
                    Toast.makeText(mContext, "得到的位置为："+position, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    class TwoDelegate implements ItemViewDelegate<MuiltEntity>
    {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.muilt_two_layout;
        }

        @Override
        public boolean isForViewType(MuiltEntity item, int position)
        {
            //判断类型是不是喽
            int type = item.type;
            if (type== MuiltEntity.TWO)
                return true;
            else
                return false;
        }

        @Override
        public void convert(ViewHolder holder, MuiltEntity muiltEntity, final int position)
        {
            //实际操作！！！
            holder.setText(R.id.tvTwo1, muiltEntity.two.model);
            holder.setText(R.id.tvTwo2, muiltEntity.two.score + "");

            holder.setOnClickListener(R.id.tvTwo1, new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Toast.makeText(mContext, "得到的位置为："+position, Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        
    }

    class ThreeDelegate implements ItemViewDelegate<MuiltEntity>
    {

        @Override
        public int getItemViewLayoutId() {
            return R.layout.muilt_three_layout;
        }

        @Override
        public boolean isForViewType(MuiltEntity item, int position)
        {
            //判断类型是不是喽
            int type = item.type;
            if (type== MuiltEntity.THREE)
                return true;
            else
                return false;
        }

        @Override
        public void convert(ViewHolder holder, MuiltEntity muiltEntity, final int position)
        {
            //实际操作！！！
            holder.setText(R.id.tvThree1, muiltEntity.three.host);
            holder.setText(R.id.tvThree2, muiltEntity.three.uid);

            holder.setOnClickListener(R.id.tvThree1, new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Toast.makeText(mContext, "得到的位置为："+position, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    //假数据
    public List<MuiltEntity> initPerson()
    {
        List<MuiltEntity> muiltList = new ArrayList<>();
        MuiltEntity muiltEntity;
        for (int i = 0; i <3 ; i++)
        {
            muiltEntity = new MuiltEntity();
            muiltEntity.type = MuiltEntity.ONE;
            muiltEntity.one = new One("炳华+i", 12 + i);
            muiltList.add(muiltEntity);
        }
        for (int i = 3; i <8 ; i++)
        {
            muiltEntity = new MuiltEntity();
            muiltEntity.type = MuiltEntity.TWO;
            muiltEntity.two = new Two(16.5d, "总共花钱" + i);
            muiltList.add(muiltEntity);
        }
        for (int i = 8; i <10 ; i++)
        {
            muiltEntity = new MuiltEntity();
            muiltEntity.type = MuiltEntity.THREE;
            muiltEntity.three = new Three("窝里都" + i, "12.58.59" + i);
            muiltList.add(muiltEntity);
        }
        return muiltList;
    }
}
