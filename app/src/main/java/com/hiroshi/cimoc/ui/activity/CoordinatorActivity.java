package com.hiroshi.cimoc.ui.activity;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/12/1.
 */

public abstract class CoordinatorActivity extends BackActivity implements
        BaseAdapter.OnItemClickListener, BaseAdapter.OnItemLongClickListener {

    @BindView(R.id.coordinator_action_button) FloatingActionButton mActionButton;
    @BindView(R.id.coordinator_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.coordinator_layout) CoordinatorLayout mLayoutView;

    @Override
    public void onItemClick(View view, int position) {}

    @Override
    public void onItemLongClick(View view, int position) {}

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_coordinator;
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

}
