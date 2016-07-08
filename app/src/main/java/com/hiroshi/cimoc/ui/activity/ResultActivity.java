package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.ResultPresenter;
import com.hiroshi.cimoc.ui.adapter.ResultAdapter;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class ResultActivity extends BaseActivity {

    public static final String EXTRA_KEYWORD = "extra_keyword";
    public static final String EXTRA_SOURCE = "extra_source";

    @BindView(R.id.result_comic_list) RecyclerView mResultList;
    @BindView(R.id.result_progress_bar) ProgressBar mProgressBar;

    private ResultAdapter mResultAdapter;
    private LinearLayoutManager mLayoutManager;
    private ResultPresenter mPresenter;

    @Override
    protected void initPresenter() {
        String keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
        int source = getIntent().getIntExtra(EXTRA_SOURCE, 0);
        mPresenter = new ResultPresenter(this, source, keyword);
    }

    @Override
    protected void initView() {
        mLayoutManager = new LinearLayoutManager(this);
        mResultAdapter = new ResultAdapter(this, new LinkedList<Comic>());
        mResultList.setLayoutManager(mLayoutManager);
        mResultList.setAdapter(mResultAdapter);
        mResultList.addItemDecoration(mResultAdapter.getItemDecoration());
        mResultList.setOnScrollListener(mPresenter.getScrollListener());
        mResultAdapter.setOnItemClickListener(mPresenter.getItemClickListener());

        mPresenter.initManga();
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activtiy_result;
    }

    @Override
    protected String getDefaultTitle() {
        return "搜索结果";
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    public void hideProgressBar() {
        if (mProgressBar.isShown()) {
            mProgressBar.setVisibility(View.GONE);
            mResultList.setVisibility(View.VISIBLE);
        }
    }

    public void addAll(List<Comic> list) {
        mResultAdapter.addAll(list);
    }

    public int findLastItemPosition() {
        return mLayoutManager.findLastVisibleItemPosition();
    }

    public int getItemCount() {
        return mLayoutManager.getItemCount();
    }

    public Comic getItem(int position) { return mResultAdapter.getItem(position); }

    public static Intent createIntent(Context context, String keyword, int source) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_KEYWORD, keyword);
        intent.putExtra(EXTRA_SOURCE, source);
        return intent;
    }

}
