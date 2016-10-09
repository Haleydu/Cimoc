package com.hiroshi.cimoc.ui.activity;

import android.graphics.Point;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.ui.adapter.ReaderAdapter;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class StreamReaderActivity extends ReaderActivity {

    private int position = 0;

    @Override
    protected void initView() {
        super.initView();
        mReaderAdapter.setReaderMode(ReaderAdapter.READER_STREAM);
        if (!CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_BLANK, false)) {
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
                        int item = mLayoutManager.findLastVisibleItemPosition();
                        if (item == mReaderAdapter.getItemCount() - 1) {
                            mPresenter.loadNext();
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
            mLayoutManager.scrollToPositionWithOffset(position + value - progress, 0);
        }
    }

    @Override
    protected void prevPage() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        if (turn == PreferenceManager.READER_TURN_ATB) {
            mRecyclerView.smoothScrollBy(0, -point.y);
        } else {
            mRecyclerView.smoothScrollBy(0, -point.x);
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
            mRecyclerView.smoothScrollBy(0, point.x);
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
    protected int getLayoutRes() {
        return R.layout.activity_stream_reader;
    }

}
