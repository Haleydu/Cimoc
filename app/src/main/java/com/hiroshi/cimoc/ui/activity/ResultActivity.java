package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.ResultPresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ResultAdapter;
import com.hiroshi.cimoc.ui.view.ResultView;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class ResultActivity extends BackActivity implements ResultView, BaseAdapter.OnItemClickListener {

    @BindView(R.id.result_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.result_layout) FrameLayout mLayoutView;

    private ResultAdapter mResultAdapter;
    private LinearLayoutManager mLayoutManager;
    private ResultPresenter mPresenter;
    private ControllerBuilderProvider mProvider;

    private int type;

    @Override
    protected BasePresenter initPresenter() {
        String keyword = getIntent().getStringExtra(Extra.EXTRA_KEYWORD);
        int[] source = getIntent().getIntArrayExtra(Extra.EXTRA_SOURCE);
        mPresenter = new ResultPresenter(source, keyword);
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
        mLayoutManager = new LinearLayoutManager(this);
        mResultAdapter = new ResultAdapter(this, new LinkedList<Comic>());
        mResultAdapter.setOnItemClickListener(this);
        mProvider = new ControllerBuilderProvider(this, SourceManager.getInstance(this).new HeaderGetter(), true);
        mResultAdapter.setProvider(mProvider);
        mResultAdapter.setTitleGetter(SourceManager.getInstance(this).new TitleGetter());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(mResultAdapter.getItemDecoration());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (mLayoutManager.findLastVisibleItemPosition() >= mResultAdapter.getItemCount() - 4 && dy > 0) {
                    load();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState){
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        mProvider.pause();
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        mProvider.resume();
                        break;
                }
            }
        });
        mRecyclerView.setAdapter(mResultAdapter);
    }

    @Override
    protected void initData() {
        type = getIntent().getIntExtra(Extra.EXTRA_MODE, -1);
        load();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProvider != null) {
            mProvider.clear();
        }
    }

    private void load() {
        switch (type) {
            case LAUNCH_MODE_SEARCH:
                mPresenter.loadSearch();
                break;
            case LAUNCH_MODE_CATEGORY:
                mPresenter.loadCategory();
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Comic comic = mResultAdapter.getItem(position);
        Intent intent = DetailActivity.createIntent(this, null, comic.getSource(), comic.getCid());
        startActivity(intent);
    }

    @Override
    public void onSearchSuccess(Comic comic) {
        hideProgressBar();
        mResultAdapter.add(comic);
    }

    @Override
    public void onLoadSuccess(List<Comic> list) {
        hideProgressBar();
        mResultAdapter.addAll(list);
    }

    @Override
    public void onLoadFail() {
        hideProgressBar();
        showSnackbar(R.string.common_parse_error);
    }

    @Override
    public void onSearchError() {
        hideProgressBar();
        showSnackbar(R.string.result_empty);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.result);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_result;
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

    @Override
    protected boolean isNavTranslation() {
        return true;
    }

    /**
     * 根据用户输入的关键词搜索
     * Extra: 关键词 图源列表
     */
    public static final int LAUNCH_MODE_SEARCH = 0;

    /**
     * 根据分类搜索，关键词字段存放 url 格式
     * Extra: 格式 图源
     */
    public static final int LAUNCH_MODE_CATEGORY = 1;

    public static Intent createIntent(Context context, String keyword, int source, int type) {
        return createIntent(context, keyword, new int[]{source}, type);
    }

    public static Intent createIntent(Context context, String keyword, int[] array, int type) {
        Intent intent = new Intent(context, ResultActivity.class);
        intent.putExtra(Extra.EXTRA_MODE, type);
        intent.putExtra(Extra.EXTRA_SOURCE, array);
        intent.putExtra(Extra.EXTRA_KEYWORD, keyword);
        return intent;
    }

}
