package com.shay.pulltorefreshview.list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.shay.base.PullToRefreshListView;
import com.shay.pulltorefreshview.R;
import com.shay.pulltorefreshview.net.Api;
import com.shay.pulltorefreshview.net.Person;
import com.shay.pulltorefreshview.widget.BaseListView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 作者：Shay-Patrick-Cormac
 * @datetime 创建时间：2017-03-31 16:12 GMT+8
 * @email 邮箱： 574583006@qq.com
 * @content 说明：创建这个类的目的，意义。
 */
public class RankList extends BaseListView<Person>
{
    int num;
    public RankList(PullToRefreshListView ptrListView, Context mContext) 
    {
        super(ptrListView, mContext);
        initListViewStart();
    }

    @Override
    public View newItemView(LayoutInflater layoutInflater, View convertView, Person person) 
    {
        View view;
        ViewHolder holder;
        // 判断convertView的状态，来达到复用效果
        if (null == convertView) {
            // 如果convertView为空，则表示第一次显示该条目，需要创建一个view
            view =layoutInflater.inflate( R.layout.list_person_layout, null);
            //新建一个viewholder对象
            holder = new ViewHolder();
            //将findviewbyID的结果赋值给holder对应的成员变量
            holder.tvPersonName = (TextView) view.findViewById(R.id.tvPersonName);
            holder.tvPersonAge = (TextView) view.findViewById(R.id.tvPersonAge);
            // 将holder与view进行绑定
            view.setTag(holder);
        } else {
            // 否则表示可以复用convertView
            view = convertView;
            holder = (ViewHolder) view.getTag();
        }
        // 直接操作holder中的成员变量即可，不需要每次都findViewById
        holder.tvPersonName.setText(person.name);
        holder.tvPersonAge.setText("年龄"+person.age);
        return view;
    }

    @Override
    public void handleResponse(String response, @ActionType int actionType) 
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
     //加载数据
        new Api(mContext,callback).getNum();
    }
    
    class ViewHolder
    {
        TextView tvPersonName;
        TextView tvPersonAge;
    }
    
    //假数据
    public List<Person> initPerson()
    {
        List<Person> personList = new ArrayList<>();
        Person person;
        for (int i = 0; i <20 ; i++) 
        {
            person = new Person();
            person.name = "炳华" + i;
            person.age = 18 + i;
            personList.add(person);
        }
        return personList;
    }
}
