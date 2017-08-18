package com.hiroshi.cimoc.ui.activity;

import android.graphics.Point;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.ui.adapter.ReaderAdapter;
import com.hiroshi.cimoc.ui.widget.ZoomableRecyclerView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class StreamReaderActivity extends ReaderActivity {

    private int mLastPosition = 0;

    @Override
    protected void initView() {
        super.initView();
        mLoadPrev = mPreference.getBoolean(PreferenceManager.PREF_READER_STREAM_LOAD_PREV, false);
        mLoadNext = mPreference.getBoolean(PreferenceManager.PREF_READER_STREAM_LOAD_NEXT, true);
        mReaderAdapter.setReaderMode(ReaderAdapter.READER_STREAM);
        if (mPreference.getBoolean(PreferenceManager.PREF_READER_STREAM_INTERVAL, false)) {
            mRecyclerView.addItemDecoration(mReaderAdapter.getItemDecoration());
        }
        ((ZoomableRecyclerView) mRecyclerView).setScaleFactor(
                mPreference.getInt(PreferenceManager.PREF_READER_SCALE_FACTOR, 200) * 0.01f);
        ((ZoomableRecyclerView) mRecyclerView).setVertical(turn == PreferenceManager.READER_TURN_ATB);
        ((ZoomableRecyclerView) mRecyclerView).setDoubleTap(
                !mPreference.getBoolean(PreferenceManager.PREF_READER_BAN_DOUBLE_CLICK, false));
        ((ZoomableRecyclerView) mRecyclerView).setTapListenerListener(this);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        hideControl();
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        if (mLoadPrev) {
                            int item = mLayoutManager.findFirstVisibleItemPosition();
                            if (item == 0) {
                                mPresenter.loadPrev();
                            }
                        }
                        if (mLoadNext) {
                            int item = mLayoutManager.findLastVisibleItemPosition();
                            if (item == mReaderAdapter.getItemCount() - 1) {
                                mPresenter.loadNext();
                            }
                        }
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int target = mLayoutManager.findFirstVisibleItemPosition();
                if (target != mLastPosition) {
                    ImageUrl newImage = mReaderAdapter.getItem(target);
                    ImageUrl oldImage = mReaderAdapter.getItem(mLastPosition);

                    if (!oldImage.getChapter().equals(newImage.getChapter())) {
                        switch (turn) {
                            case PreferenceManager.READER_TURN_ATB:
                                if (dy > 0) {
                                    mPresenter.toNextChapter();
                                } else if (dy < 0) {
                                    mPresenter.toPrevChapter();
                                }
                                break;
                            case PreferenceManager.READER_TURN_LTR:
                                if (dx > 0) {
                                    mPresenter.toNextChapter();
                                } else if (dx < 0) {
                                    mPresenter.toPrevChapter();
                                }
                                break;
                            case PreferenceManager.READER_TURN_RTL:
                                if (dx > 0) {
                                    mPresenter.toPrevChapter();
                                } else if (dx < 0) {
                                    mPresenter.toNextChapter();
                                }
                                break;
                        }
                    }
                    progress = mReaderAdapter.getItem(target).getNum();
                    mLastPosition = target;
                    updateProgress();
                }
            }
        });
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            int current = mLastPosition + value - progress;
            int pos = mReaderAdapter.getPositionByNum(current, value, value < progress);
            mLayoutManager.scrollToPositionWithOffset(pos, 0);
        }
    }

    @Override
    protected void prevPage() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        if (turn == PreferenceManager.READER_TURN_ATB) {
            mRecyclerView.smoothScrollBy(0, -point.y);
        } else {
            mRecyclerView.smoothScrollBy(-point.x, 0);
        }
        if (mLayoutManager.findFirstVisibleItemPosition() == 0) {
            loadPrev();
        }
    }

    @Override
    protected void nextPage() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        if (turn == PreferenceManager.READER_TURN_ATB) {
            mRecyclerView.smoothScrollBy(0, point.y);
        } else {
            mRecyclerView.smoothScrollBy(point.x, 0);
        }
        if (mLayoutManager.findLastVisibleItemPosition() == mReaderAdapter.getItemCount() - 1) {
            loadNext();
        }
    }

    @Override
    public void onPrevLoadSuccess(List<ImageUrl> list) {
        super.onPrevLoadSuccess(list);
        if (mLastPosition == 0) {
            mLastPosition = list.size();
        }
    }

    @Override
    protected int getCurPosition() {
        return mLastPosition;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_stream_reader;
    }

}
