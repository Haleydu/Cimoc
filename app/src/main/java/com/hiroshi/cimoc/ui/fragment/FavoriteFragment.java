package com.hiroshi.cimoc.ui.fragment;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.FavoritePresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;

import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class FavoriteFragment extends BaseFragment {

    @BindView(R.id.favorite_comic_list) RecyclerView mRecyclerView;

    private ComicAdapter mComicAdapter;
    private FavoritePresenter mPresenter;

    @Override
    protected void initView() {
        mComicAdapter = new ComicAdapter(getActivity(), mPresenter.getComic());
        mComicAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPresenter.onItemClick(position);
            }
        });
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setAdapter(mComicAdapter);
        mRecyclerView.addItemDecoration(mComicAdapter.getItemDecoration());
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_favorite;
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new FavoritePresenter(this);
    }

    public MiniComic getItem(int position) {
        return mComicAdapter.getItem(position);
    }

    public void addItem(MiniComic comic) {
        mComicAdapter.add(0, comic);
    }

    public void removeItem(long id) {
        mComicAdapter.removeById(id);
    }

    public void addItems(List<MiniComic> list) {
        mComicAdapter.addAll(0, list);
    }



}
