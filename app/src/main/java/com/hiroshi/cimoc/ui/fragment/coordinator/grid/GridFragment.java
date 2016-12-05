package com.hiroshi.cimoc.ui.fragment.coordinator.grid;

import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;
import com.hiroshi.cimoc.ui.fragment.coordinator.CoordinatorFragment;
import com.hiroshi.cimoc.ui.view.GridView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/9/22.
 */

public abstract class GridFragment extends CoordinatorFragment implements GridView {

    protected GridAdapter mGridAdapter;

    @Override
    protected BaseAdapter initAdapter() {
        mGridAdapter = new GridAdapter(getActivity(), new LinkedList<MiniComic>());
        mGridAdapter.setProvider(((CimocApplication) getActivity().getApplication()).getBuilderProvider());
        mRecyclerView.setRecycledViewPool(((CimocApplication) getActivity().getApplication()).getGridRecycledPool());
        return mGridAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        manager.setRecycleChildrenOnDetach(true);
        return manager;
    }

    @Override
    protected void initActionButton() {
        mActionButton.setImageResource(getImageRes());
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        mGridAdapter.addAll(list);
        hideProgressBar();
    }

    @Override
    public void onComicLoadFail() {
        showSnackbar(R.string.common_data_load_fail);
        hideProgressBar();
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mActionButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), accent));
    }

    protected abstract @DrawableRes int getImageRes();

}
