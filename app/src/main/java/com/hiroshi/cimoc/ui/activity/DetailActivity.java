package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.ui.adapter.ChapterAdapter;

import java.util.LinkedList;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends BaseActivity {

    public static final String EXTRA_URL = "extra_url";

    @BindView(R.id.list_chapter) RecyclerView mChapterList;

    private ChapterAdapter mChapterAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinkedList<Chapter> list = new LinkedList<>();
        for (int i = 0; i != 100; ++i) {
            list.add(new Chapter("第 " + i + " 话", ""));
        }
        mChapterAdapter = new ChapterAdapter(this, list);
        mChapterList.setLayoutManager(new GridLayoutManager(this, 4));
        mChapterList.setItemAnimator(new DefaultItemAnimator());
        mChapterList.setAdapter(mChapterAdapter);
        mChapterList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(20, 0, 20, 40);
            }
        });
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_detail;
    }

    @Override
    protected String getDefaultTitle() {
        return "详情";
    }

    public static Intent createIntent(Context context, String url) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_URL, url);
        return intent;
    }

}
