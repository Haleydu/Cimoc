package com.hiroshi.cimoc.ui.activity;

import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.ui.adapter.ReaderAdapter;
import com.hiroshi.cimoc.ui.custom.rvp.RecyclerViewPager;
import com.hiroshi.cimoc.ui.custom.rvp.RecyclerViewPager.OnPageChangedListener;
import com.hiroshi.cimoc.utils.HintUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PageReaderActivity extends ReaderActivity implements OnPageChangedListener {

    private boolean loadNext = true;
    private boolean loadPrev = true;

    @Override
    protected void initView() {
        super.initView();
        loadPrev = mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_LOAD_PREV, true);
        loadNext = mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_LOAD_NEXT, true);
        int offset = mPreference.getInt(PreferenceManager.PREF_READER_PAGE_TRIGGER, 10);
        mReaderAdapter.setReaderMode(ReaderAdapter.READER_PAGE);
        ((RecyclerViewPager) mRecyclerView).setTriggerOffset(0.01f * offset);
        ((RecyclerViewPager) mRecyclerView).addOnPageChangedListener(this);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        hideControl();
                        break;
                }
            }
        });
    }

    @Override
    public void OnPageChanged(int oldPosition, int newPosition) {
        if (oldPosition == newPosition) {
            return;
        }

        if (loadPrev && newPosition == 0) {
            mPresenter.loadPrev();
        }
        if (loadNext && newPosition == mReaderAdapter.getItemCount() - 1) {
            mPresenter.loadNext();
        }

        int offset = newPosition - oldPosition;
        if (oldPosition != -1 && offset > 0 && offset > max - progress) {
            mPresenter.toNextChapter();
        } else if (oldPosition != -1 && offset < 0 && -offset > progress - 1) {
            mPresenter.toPrevChapter();
        }
        progress = mReaderAdapter.getItem(newPosition).getNum();
        updateProgress();
    }

    @Override
    public void onPrevLoadSuccess(List<ImageUrl> list) {
        mReaderAdapter.addAll(0, list);
        ((RecyclerViewPager) mRecyclerView).refreshPosition();
        HintUtils.showToast(this, R.string.reader_load_success);
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            int current = ((RecyclerViewPager) mRecyclerView).getCurrentPosition() + value - progress;
            int pos = mReaderAdapter.getPositionByNum(current, value, value < progress);
            mRecyclerView.scrollToPosition(pos);
        }
    }

    @Override
    protected void prevPage() {
        hideControl();
        int position = ((RecyclerViewPager) mRecyclerView).getCurrentPosition();
        if (position == 0) {
            mPresenter.loadPrev();
        } else {
            mRecyclerView.smoothScrollToPosition(position - 1);
        }
    }

    @Override
    protected void nextPage() {
        hideControl();
        int position = ((RecyclerViewPager) mRecyclerView).getCurrentPosition();
        if (position == mReaderAdapter.getItemCount() - 1) {
            mPresenter.loadNext();
        } else {
            mRecyclerView.smoothScrollToPosition(position + 1);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_page_reader;
    }

}
