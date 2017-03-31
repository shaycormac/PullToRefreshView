package com.shay.pulltorefreshview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.shay.base.PullToRefreshListView;
import com.shay.pulltorefreshview.list.RankList;

public class RankListActivity extends AppCompatActivity {
    PullToRefreshListView ptrListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rank_list);
        ptrListView = (PullToRefreshListView) findViewById(R.id.ptrListView);
        new RankList(ptrListView, this);
    }
}
