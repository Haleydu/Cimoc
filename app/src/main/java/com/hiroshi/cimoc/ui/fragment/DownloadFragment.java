package com.hiroshi.cimoc.ui.fragment;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.DownloadPresenter;
import com.hiroshi.cimoc.ui.activity.TaskActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;
import com.hiroshi.cimoc.ui.view.DownloadView;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadFragment extends BaseFragment implements DownloadView {

    @BindView(R.id.download_recycler_view) RecyclerView mRecyclerView;

    private ComicAdapter mComicAdapter;
    private DownloadPresenter mPresenter;

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_download;
    }

    @Override
    protected void initView() {
        mComicAdapter = new ComicAdapter(getActivity(), new LinkedList<MiniComic>());
        mComicAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                MiniComic comic = mComicAdapter.getItem(position);
                Intent intent =
                        TaskActivity.createIntent(getActivity(), comic.getId(), comic.getSource(), comic.getCid(), comic.getTitle());
                startActivity(intent);
            }
        });
        mComicAdapter.setProvider(new ControllerBuilderProvider(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setAdapter(mComicAdapter);
        mRecyclerView.addItemDecoration(mComicAdapter.getItemDecoration());
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    protected void initPresenter() {
        mPresenter = new DownloadPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        super.onDestroyView();
    }

    @Override
    public void onLoadSuccess(List<MiniComic> list) {
        mComicAdapter.addAll(list);
    }

}
