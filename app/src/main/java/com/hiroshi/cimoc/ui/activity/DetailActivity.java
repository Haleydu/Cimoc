package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.ui.adapter.ChapterAdapter;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends BaseActivity {

    public static final String EXTRA_SOURCE = "extra_source";
    public static final String EXTRA_PATH = "extra_path";

    @BindView(R.id.detail_chapter_list) RecyclerView mChapterList;

    private ChapterAdapter mChapterAdapter;
    private DetailPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String path = getIntent().getStringExtra(EXTRA_PATH);
        int source = getIntent().getIntExtra(EXTRA_SOURCE, 0);
        mPresenter.loadComic(path, source);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new DetailPresenter(this);
    }

    @Override
    protected void initView() {
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
        return null;
    }

    public void setChapterList(List<Chapter> list, String image, String title, String intro) {
        mChapterAdapter = new ChapterAdapter(this, list, image, title, intro);
        mChapterList.setLayoutManager(new GridLayoutManager(this, 4));
        mChapterList.setAdapter(mChapterAdapter);
        mChapterList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                int position = parent.getChildLayoutPosition(view);
                if (position == 0) {
                    outRect.set(0, 0, 0, 60);
                } else {
                    outRect.set(20, 0, 20, 40);
                }
            }
        });
    }

    public static Intent createIntent(Context context, String path, int source) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_PATH, path);
        intent.putExtra(EXTRA_SOURCE, source);
        return intent;
    }

}
