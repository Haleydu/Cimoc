package com.hiroshi.cimoc.ui.fragment;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class StarFragment extends BaseFragment {

    @BindView(R.id.comic_list) RecyclerView mComicList;

    private ComicAdapter mComicAdapter;

    @Override
    protected void initView() {
        List<Comic> list = new LinkedList<>();
        mComicAdapter = new ComicAdapter(getActivity(), list);
        mComicList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mComicList.setItemAnimator(new DefaultItemAnimator());
        mComicList.setAdapter(mComicAdapter);
        mComicList.addItemDecoration(mComicAdapter.getItemDecoration());
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_star;
    }
}
