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

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.ui.adapter.ChapterAdapter;
import com.hiroshi.cimoc.utils.ImagePipelineConfigFactory;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends BaseActivity {

    @BindView(R.id.detail_chapter_list) RecyclerView mChapterList;
    @BindView(R.id.detail_coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.detail_star_btn) FloatingActionButton mStarButton;
    @BindView(R.id.detail_progress_bar) ProgressBar mProgressBar;

    private ChapterAdapter mChapterAdapter;
    private GridLayoutManager mLayoutManager;
    private DetailPresenter mPresenter;

    private int lastChapter;

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
    protected void initPresenter() {
        mPresenter = new DetailPresenter(this);
    }

    @Override
    protected void initView() {
        mPresenter.loadComic();
        Fresco.initialize(getApplicationContext(), ImagePipelineConfigFactory.getImagePipelineConfig(getApplicationContext(), mPresenter.getSource()));
    }

    @Override
    protected void onDestroy() {
        mPresenter.saveComic();
        super.onDestroy();
        Fresco.initialize(getApplicationContext(), ImagePipelineConfigFactory.getImagePipelineConfig(getApplicationContext()));
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

    public void setLastChapter(String path) {
        int nextChapter = mChapterAdapter.getPositionByPath(path);
        if (lastChapter != nextChapter && nextChapter != -1) {
            ChapterAdapter.ViewHolder viewHolder = (ChapterAdapter.ViewHolder) mChapterList.findViewHolderForLayoutPosition(nextChapter);
            viewHolder.select();
            if (lastChapter != -1) {
                viewHolder = (ChapterAdapter.ViewHolder) mChapterList.findViewHolderForLayoutPosition(lastChapter);
                viewHolder.clear();
            }
            lastChapter = nextChapter;
        }
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

    public void setChapterList(Comic comic, List<Chapter> list, String last) {
        mChapterAdapter = new ChapterAdapter(this, list, comic.getImage(), comic.getTitle(),
                comic.getAuthor(), comic.getIntro(), comic.getStatus(), comic.getUpdate(), last);
        mChapterAdapter.setOnItemClickListener(mPresenter.getOnClickListener());
        mLayoutManager = new GridLayoutManager(this, 4);
        mChapterList.setLayoutManager(mLayoutManager);
        mChapterList.setAdapter(mChapterAdapter);
        mChapterList.addItemDecoration(mChapterAdapter.getItemDecoration());
        lastChapter = mChapterAdapter.getPositionByPath(last);
    }

    public List<Chapter> getChapter() {
        return mChapterAdapter.getDataSet();
    }

    public static Intent createIntent(Context context, int source, String path) {
        ComicManager.getInstance().initComic(source, path);
        return new Intent(context, DetailActivity.class);
    }

}
