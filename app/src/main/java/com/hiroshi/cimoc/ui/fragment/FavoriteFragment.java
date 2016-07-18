package com.hiroshi.cimoc.ui.fragment;

import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.FavoritePresenter;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;
import com.hiroshi.db.entity.ComicRecord;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class FavoriteFragment extends BaseFragment {

    @BindView(R.id.favorite_comic_list) RecyclerView mComicList;

    private ComicAdapter mComicAdapter;
    private FavoritePresenter mPresenter;

    @Override
    protected void initView() {
        mComicAdapter = new ComicAdapter(getActivity(), mPresenter.getComicRecord());
        mComicAdapter.setOnItemClickListener(mPresenter.getItemClickListener());
        mComicList.setItemAnimator(null);
        mComicList.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mComicList.setItemAnimator(new DefaultItemAnimator());
        mComicList.setAdapter(mComicAdapter);
        mComicList.addItemDecoration(mComicAdapter.getItemDecoration());
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

    public ComicRecord getItem(int position) {
        return mComicAdapter.getItem(position);
    }

    public void addItem(ComicRecord comic) {
        mComicAdapter.add(0, comic);
    }

    public void removeItem(long id) {
        mComicAdapter.removeById(id);
    }

}
