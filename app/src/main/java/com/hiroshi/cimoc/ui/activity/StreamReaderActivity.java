package com.hiroshi.cimoc.ui.activity;

import android.graphics.Point;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.ui.adapter.ReaderAdapter;
import com.hiroshi.cimoc.ui.custom.rvp.RecyclerViewPager;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class StreamReaderActivity extends ReaderActivity {

    private int position = 0;
    private boolean loadNext = true;
    private boolean loadPrev = false;

    @Override
    protected void initView() {
        super.initView();
        loadPrev = mPreference.getBoolean(PreferenceManager.PREF_READER_STREAM_LOAD_PREV, false);
        loadNext = mPreference.getBoolean(PreferenceManager.PREF_READER_STREAM_LOAD_NEXT, true);
        mReaderAdapter.setReaderMode(ReaderAdapter.READER_STREAM);
        if (mPreference.getBoolean(PreferenceManager.PREF_READER_STREAM_INTERVAL, false)) {
            mRecyclerView.addItemDecoration(mReaderAdapter.getItemDecoration());
        }
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        hideControl();
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        if (loadPrev) {
                            int item = mLayoutManager.findFirstVisibleItemPosition();
                            if (item == 0) {
                                mPresenter.loadPrev();
                            }
                        }
                        if (loadNext) {
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
                int item;
                switch(turn) {
                    default:
                    case PreferenceManager.READER_TURN_LTR:
                        item = mLayoutManager.findFirstVisibleItemPosition();
                        if (item != position) {
                            if (dx > 0 && progress == max) {
                                mPresenter.toNextChapter();
                            } else if (dx < 0 && progress == 1) {
                                mPresenter.toPrevChapter();
                            }
                        }
                        break;
                    case PreferenceManager.READER_TURN_RTL:
                        item = mLayoutManager.findFirstVisibleItemPosition();
                        if (item != position) {
                            if (dx < 0 && progress == max) {
                                mPresenter.toNextChapter();
                            } else if (dx > 0 && progress == 1) {
                                mPresenter.toPrevChapter();
                            }
                        }
                        break;
                    case PreferenceManager.READER_TURN_ATB:
                        item = mLayoutManager.findFirstVisibleItemPosition();
                        if (item != position) {
                            if (dy > 0 && progress == max) {
                                mPresenter.toNextChapter();
                            } else if (dy < 0 && progress == 1) {
                                mPresenter.toPrevChapter();
                            }
                        }
                        break;
                }
                if (item != position) {
                    progress = mReaderAdapter.getItem(item).getNum();
                    position = item;
                    updateProgress();
                }
            }
        });
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            int current = position + value - progress;
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
        if (position == 0) {
            position = list.size();
        }
    }

    @Override
    protected int getCurPosition() {
        return position;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_stream_reader;
    }

}
