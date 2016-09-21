package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.ResultPresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ResultAdapter;
import com.hiroshi.cimoc.ui.view.ResultView;

import java.util.LinkedList;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class ResultActivity extends BackActivity implements ResultView, BaseAdapter.OnItemClickListener {

    @BindView(R.id.result_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.result_layout) LinearLayout mLinearLayout;

    private ResultAdapter mResultAdapter;
    private LinearLayoutManager mLayoutManager;
    private ResultPresenter mPresenter;

    @Override
    protected void initPresenter() {
        String keyword = getIntent().getStringExtra(EXTRA_KEYWORD);
        int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        mPresenter = new ResultPresenter(new int[] {0, 1, 2, 3}, keyword);
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mLayoutManager = new LinearLayoutManager(this);
        mResultAdapter = new ResultAdapter(this, new LinkedList<Comic>());
        mResultAdapter.setOnItemClickListener(this);
        mResultAdapter.setProvider(new ControllerBuilderProvider(this));
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
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onItemClick(View view, int position) {
        Comic comic = mResultAdapter.getItem(position);
        Intent intent = DetailActivity.createIntent(this, comic.getId(), comic.getSource(), comic.getCid());
        startActivity(intent);
    }

    @Override
    public void onLoadSuccess(Comic comic) {
        hideProgressBar();
        mResultAdapter.add(comic);
    }

    @Override
    public void onNetworkError() {
        hideProgressBar();
        showSnackbar(R.string.common_network_error);
    }

    @Override
    public void onParseError() {
        hideProgressBar();
        showSnackbar(R.string.common_parse_error);
    }

    @Override
    public void onEmptyResult() {
        hideProgressBar();
        showSnackbar(R.string.result_empty);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activtiy_result;
    }

    @Override
    protected View getLayoutView() {
        return mLinearLayout;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.result);
    }

    public static final String EXTRA_KEYWORD = "a";
    public static final String EXTRA_SOURCE = "b";

    public static Intent createIntent(Context context, String keyword, int sid) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(EXTRA_KEYWORD, keyword);
        intent.putExtra(EXTRA_SOURCE, sid);
        return intent;
    }

}
