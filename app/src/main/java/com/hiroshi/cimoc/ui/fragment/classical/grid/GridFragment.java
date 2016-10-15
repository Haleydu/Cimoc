package com.hiroshi.cimoc.ui.fragment.classical.grid;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;
import com.hiroshi.cimoc.ui.fragment.classical.ClassicalFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.GridView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/9/22.
 */

public abstract class GridFragment extends ClassicalFragment implements GridView, MessageDialogFragment.MessageDialogListener {

    protected GridAdapter mGridAdapter;

    @Override
    protected void initView() {
        mGridAdapter = new GridAdapter(getActivity(), new LinkedList<MiniComic>());
        mGridAdapter.setProvider(((CimocApplication) getActivity().getApplication()).getBuilderProvider());
        mRecyclerView.setRecycledViewPool(((CimocApplication) getActivity().getApplication()).getGridRecycledPool());
        super.initView();
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        mGridAdapter.addAll(list);
    }

    @Override
    public void onComicLoadFail() {
        showSnackbar(R.string.common_data_load_fail);
    }

    @Override
    public void onComicFilterSuccess(List<MiniComic> list) {
        mGridAdapter.setData(list);
    }

    @Override
    public void onComicFilterFail() {
        showSnackbar(R.string.comic_filter_fail);
    }

    @Override
    protected BaseAdapter getAdapter() {
        return mGridAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        manager.setRecycleChildrenOnDetach(true);
        return manager;
    }

}
