package com.hiroshi.cimoc.ui.fragment.coordinator.grid;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;
import com.hiroshi.cimoc.ui.fragment.coordinator.RecyclerViewFragment;
import com.hiroshi.cimoc.ui.view.GridView;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/9/22.
 */

public abstract class GridFragment extends RecyclerViewFragment implements GridView {

    protected GridAdapter mGridAdapter;

    @Override
    protected BaseAdapter initAdapter() {
        mGridAdapter = new GridAdapter(getActivity(), new LinkedList<MiniComic>());
        mGridAdapter.setProvider(getAppInstance().getBuilderProvider());
        mRecyclerView.setRecycledViewPool(getAppInstance().getGridRecycledPool());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState){
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        getAppInstance().getBuilderProvider().pause();
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        getAppInstance().getBuilderProvider().resume();
                        break;
                }
            }
        });
        return mGridAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        manager.setRecycleChildrenOnDetach(true);
        return manager;
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

}
