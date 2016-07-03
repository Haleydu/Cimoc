package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.Administrator;
import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.ui.adapter.ResultAdapter;
import com.hiroshi.cimoc.utils.EventMessage;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class ResultActivity extends BaseActivity {

    public static final String EXTRA_KEYWORD = "extra_keyword";
    public static final String EXTRA_SOURCE = "extra_source";

    @BindView(R.id.result_list) RecyclerView mResultList;

    private ResultAdapter mResultAdapter;
    private Manga mManga;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initList();
        initManga();
        isLoading = false;
    }

    private void initList() {
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mResultAdapter = new ResultAdapter(this, new LinkedList<MiniComic>());
        mResultList.setLayoutManager(mLayoutManager);
        mResultList.setItemAnimator(new DefaultItemAnimator());
        mResultList.setAdapter(mResultAdapter);
        mResultList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                outRect.set(0, 0, 0, 10);
            }
        });
        mResultList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastItem = mLayoutManager.findLastVisibleItemPosition();
                int itemCount = mLayoutManager.getItemCount();
                if (lastItem >= itemCount - 4 && dy > 0) {
                    if (!isLoading) {
                        isLoading = true;
                        mManga.searchNext();
                    }
                }
            }
        });
    }

    private void initManga() {
        String keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
        mManga = Administrator.getMangaById(getIntent().getIntExtra(EXTRA_SOURCE, 0));
        mManga.searchFirst(keyword);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activtiy_result;
    }

    @Override
    protected String getDefaultTitle() {
        return "搜索结果";
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.SEARCH_SUCCESS:
                mResultAdapter.addAll((List<MiniComic>) msg.getData());
                isLoading = false;
                break;
            case EventMessage.SEARCH_EMPTY:
                break;
        }
    }

    public static Intent createIntent(Context context, String keyword, int source) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_KEYWORD, keyword);
        intent.putExtra(EXTRA_SOURCE, source);
        return intent;
    }

}
