package com.hiroshi.cimoc.ui.activity;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/12/10.
 */

public abstract class RecyclerActivity extends BackActivity implements
        BaseAdapter.OnItemClickListener, BaseAdapter.OnItemLongClickListener {

    @BindView(R.id.recycler_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.recycler_layout) FrameLayout mLayoutView;

    @Override
    public void onItemClick(View view, int position) {}

    @Override
    public void onItemLongClick(View view, int position) {}

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_recycler;
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

}
