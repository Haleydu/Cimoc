package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ChapterAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends BaseActivity {

    @BindView(R.id.detail_chapter_list) RecyclerView mRecyclerView;
    @BindView(R.id.detail_coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.detail_star_btn) FloatingActionButton mStarButton;
    @BindView(R.id.detail_progress_bar) ProgressBar mProgressBar;

    private ChapterAdapter mChapterAdapter;
    private DetailPresenter mPresenter;

    @Override
    public void onBackPressed() {
        mPresenter.saveComic();
        super.onBackPressed();
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void initView() {}

    @Override
    protected void initPresenter() {
        mPresenter = new DetailPresenter(this);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_detail;
    }

    @Override
    protected String getDefaultTitle() {
        return "详情";
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @OnClick(R.id.detail_star_btn) void onClick() {
        mPresenter.onStarClick();
    }

    public void setStarButtonRes(int resId) {
        mStarButton.setImageResource(resId);
    }

    public void setStarButtonVisible() {
        mStarButton.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
        mCoordinatorLayout.setVisibility(View.VISIBLE);
    }

    public void showSnackbar(String msg) {
        Snackbar.make(mCoordinatorLayout, msg, Snackbar.LENGTH_SHORT).show();
    }

    public void setLastChapter(String last) {
        mChapterAdapter.setLast(last);
    }

    public void initRecyclerView(Comic comic, List<Chapter> list, String last) {
        mChapterAdapter = new ChapterAdapter(this, list, comic.getCover(), comic.getTitle(),
                comic.getAuthor(), comic.getIntro(), comic.getStatus(), comic.getUpdate());
        mChapterAdapter.setLast(last);
        mChapterAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPresenter.onItemClick(position);
            }
        });
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mRecyclerView.setAdapter(mChapterAdapter);
        mRecyclerView.addItemDecoration(mChapterAdapter.getItemDecoration());
    }

    public static Intent createIntent(Context context, Long id, int source, String cid) {
        ComicManager.getInstance().setComic(id, source, cid);
        return new Intent(context, DetailActivity.class);
    }

}
