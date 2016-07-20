package com.hiroshi.cimoc.ui.fragment;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.HistoryPresenter;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;


import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class HistoryFragment extends BaseFragment {

    @BindView(R.id.history_comic_list) RecyclerView mComicList;

    private ComicAdapter mComicAdapter;
    private HistoryPresenter mPresenter;

    @Override
    protected void initView() {
        mComicAdapter = new ComicAdapter(getActivity(), mPresenter.getComic());
        mComicAdapter.setOnItemClickListener(mPresenter.getItemClickListener());
        mComicList.setItemAnimator(null);
        mComicList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mComicList.setItemAnimator(new DefaultItemAnimator());
        mComicList.setAdapter(mComicAdapter);
        mComicList.addItemDecoration(mComicAdapter.getItemDecoration());
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new HistoryPresenter(this);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_history;
    }

    public Comic getItem(int position) {
        return mComicAdapter.getItem(position);
    }

    public void updateItem(Comic comic) {
        mComicAdapter.update(0, comic);
    }

}
