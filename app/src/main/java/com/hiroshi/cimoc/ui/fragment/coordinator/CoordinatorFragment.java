package com.hiroshi.cimoc.ui.fragment.coordinator;

import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public abstract class CoordinatorFragment extends BaseFragment implements BaseAdapter.OnItemClickListener,
        BaseAdapter.OnItemLongClickListener {

    @BindView(R.id.coordinator_recycler_view) protected RecyclerView mRecyclerView;
    @BindView(R.id.coordinator_action_button) protected FloatingActionButton mActionButton;
    @BindView(R.id.coordinator_layout) protected CoordinatorLayout mLayoutView;

    @Override
    protected void initView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(initLayoutManager());
        BaseAdapter adapter = initAdapter();
        if (adapter != null) {
            adapter.setOnItemClickListener(this);
            adapter.setOnItemLongClickListener(this);
            mRecyclerView.addItemDecoration(adapter.getItemDecoration());
            mRecyclerView.setAdapter(adapter);
        }
        initActionButton();
    }

    abstract protected BaseAdapter initAdapter();

    protected abstract RecyclerView.LayoutManager initLayoutManager();

    abstract protected void initActionButton();

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (mActionButton != null) {
            if (hidden) {
                mActionButton.hide();
            } else {
                mActionButton.show();
            }
        }
    }

    @Override
    public void onItemClick(View view, int position) {}

    @Override
    public void onItemLongClick(View view, int position) {}

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_coordinator;
    }

}
