package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.StreamReaderPresenter;
import com.hiroshi.cimoc.ui.adapter.PictureStreamAdapter;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.Arrays;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class StreamReaderActivity  extends BaseActivity {

    public static final String EXTRA_POSITION = "extra_position";

    @BindView(R.id.reader_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.reader_chapter_title) TextView mChapterTitle;
    @BindView(R.id.reader_chapter_page) TextView mChapterPage;
    @BindView(R.id.reader_tool_bar) LinearLayout mToolLayout;
    @BindView(R.id.reader_seek_bar) DiscreteSeekBar mSeekBar;

    private PictureStreamAdapter mStreamAdapter;
    private StreamReaderPresenter mPresenter;
    private LinearLayoutManager mLayoutManager;

    private int progress;

    @Override
    public void onBackPressed() {
        mPresenter.afterRead(mSeekBar.getProgress());
        super.onBackPressed();
    }

    @Override
    protected void initView() {
        progress = 0;
        mLayoutManager = new LinearLayoutManager(this);
        mStreamAdapter = new PictureStreamAdapter(this, ControllerBuilderFactory.getControllerBuilder(mPresenter.getSource(), this));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mStreamAdapter);
        mRecyclerView.addItemDecoration(mStreamAdapter.getItemDecoration());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mPresenter.onScrolled(dy, mLayoutManager.findLastVisibleItemPosition(), mStreamAdapter.getItemCount());
            }
        });
    }

    @Override
    protected void initToolbar() {}

    @Override
    protected void initPresenter() {
        int position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        mPresenter = new StreamReaderPresenter(this, position);
    }

    @Override
    protected String getDefaultTitle() {
        return null;
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_stream_reader;
    }

    public void hideChapterInfo() {
        mToolLayout.setVisibility(View.GONE);
        mChapterPage.setText(null);
        mChapterTitle.setText(null);
    }

    public void setPrevImage(String[] array) {
        mStreamAdapter.addAll(0, Arrays.asList(array));
    }

    public void setNextImage(String[] array) {
        mStreamAdapter.addAll(Arrays.asList(array));
    }

    public void updateChapterInfo(int max, String title) {
        mSeekBar.setMax(max);
        mChapterTitle.setText(title);
    }

    public void setReadProgress(int value) {
        mSeekBar.setProgress(value);
        String pageString = value + "/" + mSeekBar.getMax();
        mChapterPage.setText(pageString);
        progress = value;
    }

    public static Intent createIntent(Context context, int position) {
        Intent intent = new Intent(context, StreamReaderActivity.class);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

}
