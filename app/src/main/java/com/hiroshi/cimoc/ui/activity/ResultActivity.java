package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.ResultPresenter;
import com.hiroshi.cimoc.ui.adapter.ResultAdapter;
import com.hiroshi.cimoc.ui.view.ResultView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class ResultActivity extends CoordinatorActivity implements ResultView {

    private ResultAdapter mResultAdapter;
    private LinearLayoutManager mLayoutManager;
    private ResultPresenter mPresenter;
    private ControllerBuilderProvider mProvider;

    @Override
    protected void initPresenter() {
        String keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
        ArrayList<Integer> source = getIntent().getIntegerArrayListExtra(EXTRA_SOURCE);
        mPresenter = new ResultPresenter(source, keyword);
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mLayoutManager = new LinearLayoutManager(this);
        mResultAdapter = new ResultAdapter(this, new LinkedList<Comic>());
        mResultAdapter.setOnItemClickListener(this);
        mProvider = new ControllerBuilderProvider(this);
        mResultAdapter.setProvider(mProvider);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(mResultAdapter.getItemDecoration());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mLayoutManager.findLastVisibleItemPosition() >= mResultAdapter.getItemCount() - 4 && dy > 0) {
                    mPresenter.load();
                }
            }
        });
        mRecyclerView.setAdapter(mResultAdapter);
        mLayoutView.removeView(mActionButton);
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
        if (mProvider != null) {
            mProvider.clear();
            mProvider = null;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Comic comic = mResultAdapter.getItem(position);
        Intent intent = DetailActivity.createIntent(this, null, comic.getSource(), comic.getCid(), false);
        startActivity(intent);
    }

    @Override
    public void onSearchSuccess(Comic comic) {
        hideProgressBar();
        mResultAdapter.add(comic);
    }

    @Override
    public void onRecentLoadSuccess(List<Comic> list) {
        hideProgressBar();
        mResultAdapter.addAll(list);
    }

    @Override
    public void onRecentLoadFail() {
        hideProgressBar();
        showSnackbar(R.string.common_parse_error);
    }

    @Override
    public void onResultEmpty() {
        hideProgressBar();
        showSnackbar(R.string.result_empty);
    }

    @Override
    public void onSearchError() {
        hideProgressBar();
        showSnackbar(R.string.result_error);
    }

    @Override
    protected String getDefaultTitle() {
        return getIntent().getStringExtra(EXTRA_KEYWORD) == null ? getString(R.string.result_recent) : getString(R.string.result);
    }

    public static final String EXTRA_KEYWORD = "a";
    public static final String EXTRA_SOURCE = "b";

    public static Intent createIntent(Context context, int source) {
        Intent intent = new Intent(context, ResultActivity.class);
        ArrayList<Integer> list = new ArrayList<>(1);
        list.add(source);
        intent.putIntegerArrayListExtra(EXTRA_SOURCE, list);
        return intent;
    }

    public static Intent createIntent(Context context, String keyword, ArrayList<Integer> list) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_KEYWORD, keyword);
        intent.putIntegerArrayListExtra(EXTRA_SOURCE, list);
        return intent;
    }

}
