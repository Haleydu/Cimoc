package com.hiroshi.cimoc.ui.fragment.classical;

import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.fragment.BaseFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public abstract class ClassicalFragment extends BaseFragment implements BaseAdapter.OnItemClickListener,
        BaseAdapter.OnItemLongClickListener {

    @BindView(R.id.classical_recycler_view) protected RecyclerView mRecyclerView;
    @BindView(R.id.classical_action_button) protected FloatingActionButton mActionButton;

    @Override
    protected void initView() {
        mActionButton.setImageResource(getImageRes());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(getLayoutManager());
        BaseAdapter adapter = getAdapter();
        if (adapter != null) {
            adapter.setOnItemClickListener(this);
            adapter.setOnItemLongClickListener(this);
            mRecyclerView.addItemDecoration(adapter.getItemDecoration());
            mRecyclerView.setAdapter(getAdapter());
        }
    }

    @OnClick(R.id.classical_action_button) void onFloatActionButtonClick() {
        onActionButtonClick();
    }

    @Override
    public void onItemClick(View view, int position) {}

    @Override
    public void onItemLongClick(View view, int position) {}

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_classical;
    }

    protected abstract void onActionButtonClick();

    protected abstract int getImageRes();

    protected abstract RecyclerView.LayoutManager getLayoutManager();

    protected abstract BaseAdapter getAdapter();

}
