package com.shay.pulltorefreshview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;

import com.shay.base.PullToRefreshListView;
import com.shay.pulltorefreshview.list.MuiltTypeList;

public class RankListActivity extends AppCompatActivity {
    PullToRefreshListView ptrListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank_list);
        ptrListView = (PullToRefreshListView) findViewById(R.id.ptrListView);
        //new RankList(ptrListView, this);
        View headView = LayoutInflater.from(this).inflate(R.layout.muilt_headview_layout, null);
        new MuiltTypeList(ptrListView, this,headView);
    }
}
