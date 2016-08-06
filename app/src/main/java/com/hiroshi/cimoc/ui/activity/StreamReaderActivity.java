package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.StreamReaderPresenter;
import com.hiroshi.cimoc.ui.adapter.PictureStreamAdapter;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class StreamReaderActivity extends ReaderActivity{

    @BindView(R.id.reader_recycler_view) RecyclerView mRecyclerView;

    private PictureStreamAdapter mStreamAdapter;
    private StreamReaderPresenter mPresenter;
    private LinearLayoutManager mLayoutManager;

    private int position;

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.setPage(mSeekBar.getProgress());
    }

    @Override
    protected void initView() {
        progress = 1;
        mLayoutManager = new LinearLayoutManager(this);
        mStreamAdapter = new PictureStreamAdapter(this, ControllerBuilderFactory.getControllerBuilder(getIntent().getIntExtra(EXTRA_SOURCE, -1), this));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mStreamAdapter);
        mRecyclerView.addItemDecoration(mStreamAdapter.getItemDecoration());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int item = mLayoutManager.findFirstVisibleItemPosition();
                if (item != position) {
                    position = item;
                    if (dy > 0) {
                        ++progress;
                        if (progress == mSeekBar.getMax()) {

                        }
                    } else {
                        --progress;
                    }
                    mPresenter.onScrolled(dy, position, mStreamAdapter.getItemCount());
                }

                Log.e("--------------", "-----" + position);
            }
        });
    }

    @Override
    protected void initPresenter() {
        int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        String cid = getIntent().getStringExtra(EXTRA_CID);
        String last = getIntent().getStringExtra(EXTRA_LAST);
        int page = getIntent().getIntExtra(EXTRA_PAGE, -1);
        String[] title = getIntent().getStringArrayExtra(EXTRA_TITLE);
        String[] path = getIntent().getStringArrayExtra(EXTRA_PATH);
        Chapter[] array = fromArray(title, path);
        int position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        mPresenter = new StreamReaderPresenter(this, source, cid, last, page, array, position);
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected int getLayoutRes() {
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

    public static Intent createIntent(Context context, Comic comic, List<Chapter> list, int position) {
        Intent intent = new Intent(context, StreamReaderActivity.class);
        putExtras(intent, comic, list, position);
        return intent;
    }

}
