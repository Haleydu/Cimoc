package com.haleydu.cimoc.ui.activity;

import android.os.Build;

import androidx.recyclerview.widget.RecyclerView;

import com.haleydu.cimoc.R;
import com.haleydu.cimoc.manager.PreferenceManager;
import com.haleydu.cimoc.model.ImageUrl;
import com.haleydu.cimoc.ui.adapter.ReaderAdapter;
import com.haleydu.cimoc.ui.widget.rvp.RecyclerViewPager;
import com.haleydu.cimoc.ui.widget.rvp.RecyclerViewPager.OnPageChangedListener;
import com.haleydu.cimoc.utils.HintUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PageReaderActivity extends ReaderActivity implements OnPageChangedListener {

    @Override
    protected void initView() {
        super.initView();
        mLoadPrev = mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_LOAD_PREV, true);
        mLoadNext = mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_LOAD_NEXT, true);
        int offset = mPreference.getInt(PreferenceManager.PREF_READER_PAGE_TRIGGER, 10);
        mReaderAdapter.setReaderMode(ReaderAdapter.READER_PAGE);
        if (mPreference.getBoolean(PreferenceManager.PREF_READER_PAGE_QUICK_TURN, false)) {
            ((RecyclerViewPager) mRecyclerView).setScrollSpeed(0.000001f);
        } else {
            ((RecyclerViewPager) mRecyclerView).setScrollSpeed(0.12f);
        }
        ((RecyclerViewPager) mRecyclerView).setTriggerOffset(0.01f * offset);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            mRecyclerView.setForceDarkAllowed(false);
        }
        ((RecyclerViewPager) mRecyclerView).setOnPageChangedListener(this);
        mRecyclerView.setItemAnimator(null);
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
        if (oldPosition < 0 || newPosition < 0) {
            return;
        }

        if (mLoadPrev && newPosition == 0) {
            mPresenter.loadPrev();
        }
        if (mLoadNext && newPosition == mReaderAdapter.getItemCount() - 1) {
            mPresenter.loadNext();
        }

        ImageUrl newImage = mReaderAdapter.getItem(newPosition);
        ImageUrl oldImage = mReaderAdapter.getItem(oldPosition);

        if (!oldImage.getChapter().equals(newImage.getChapter())) {
            if (newPosition > oldPosition) {
                mPresenter.toNextChapter();
            } else if (newPosition < oldPosition) {
                mPresenter.toPrevChapter();
            }
        }

        progress = newImage.getNum();
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
            int current = getCurPosition() + value - progress;
            int pos = mReaderAdapter.getPositionByNum(current, value, value < progress);
            mRecyclerView.scrollToPosition(pos);
        }
    }

    @Override
    protected void prevPage() {
        hideControl();
        int position = getCurPosition();
        if (position == 0) {
            mPresenter.loadPrev();
        } else {
            mRecyclerView.smoothScrollToPosition(position - 1);
        }
    }

    @Override
    protected void nextPage() {
        hideControl();
        int position = getCurPosition();
        if (position == mReaderAdapter.getItemCount() - 1) {
            mPresenter.loadNext();
        } else {
            mRecyclerView.smoothScrollToPosition(position + 1);
        }
    }

    @Override
    protected int getCurPosition() {
        return ((RecyclerViewPager) mRecyclerView).getCurrentPosition();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_page_reader;
    }

}
