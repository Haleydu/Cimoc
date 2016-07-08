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
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.ui.adapter.ChapterAdapter;
import com.hiroshi.db.entity.FavoriteComic;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends BaseActivity {

    public static final String EXTRA_SOURCE = "extra_source";
    public static final String EXTRA_PATH = "extra_path";

    @BindView(R.id.detail_chapter_list) RecyclerView mChapterList;
    @BindView(R.id.detail_coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.detail_star_btn) FloatingActionButton mStarButton;
    @BindView(R.id.detail_progress_bar) ProgressBar mProgressBar;

    private ChapterAdapter mChapterAdapter;
    private DetailPresenter mPresenter;

    private int source;
    private String path;

    @Override
    protected void initPresenter() {
        mPresenter = new DetailPresenter(this);
    }

    @Override
    protected void initView() {
        path = getIntent().getStringExtra(EXTRA_PATH);
        source = getIntent().getIntExtra(EXTRA_SOURCE, 0);
        mPresenter.loadComic(path, source);
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
        mPresenter.onStarClick(source, path);
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

    public void setChapterList(Comic comic, List<Chapter> list) {
        mChapterAdapter = new ChapterAdapter(this, list, comic.getImage(), comic.getTitle(),
                comic.getAuthor(), comic.getIntro(), comic.getStatus(), comic.getUpdate());
        mChapterList.setLayoutManager(new GridLayoutManager(this, 4));
        mChapterList.setAdapter(mChapterAdapter);
        mChapterList.addItemDecoration(mChapterAdapter.getItemDecoration());
    }

    public static Intent createIntent(Context context, Comic comic) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_PATH, comic.getPath());
        intent.putExtra(EXTRA_SOURCE, comic.getSource());
        return intent;
    }

    public static Intent createIntent(Context context, FavoriteComic comic) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_PATH, comic.getPath());
        intent.putExtra(EXTRA_SOURCE, comic.getSource());
        return intent;
    }

}
