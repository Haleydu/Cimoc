package com.hiroshi.cimoc.ui.activity;

import android.graphics.Point;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.PreferenceMaster;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.ui.custom.rvp.RecyclerViewPager;
import com.hiroshi.cimoc.ui.custom.rvp.RecyclerViewPager.OnPageChangedListener;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PageReaderActivity extends ReaderActivity implements OnPageChangedListener {

    @BindView(R.id.reader_recycler_view) RecyclerViewPager mRecyclerView;

    private boolean volume;
    private boolean reverse;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (volume) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    mRecyclerView.scrollToPosition(mRecyclerView.getCurrentPosition() - 1);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    mRecyclerView.scrollToPosition(mRecyclerView.getCurrentPosition() + 1);
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void initView() {
        super.initView();
        reverse = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_REVERSE, false);
        volume = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_VOLUME, false);
        mSeekBar.setReverse(reverse);
        mLayoutManager.setExtraSpace(4);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mLayoutManager.setReverseLayout(reverse);
        mReaderAdapter.setAutoSplit(false);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mReaderAdapter);
        mRecyclerView.addOnPageChangedListener(this);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        hideToolLayout();
                        break;
                }
            }
        });
    }

    @Override
    public void OnPageChanged(int oldPosition, int newPosition) {
        if (newPosition > oldPosition && progress == max) {
            mPresenter.toNextChapter();
        } else if (newPosition < oldPosition && progress == 1) {
            mPresenter.toPrevChapter();
        } else {
            setReadProgress(progress + newPosition - oldPosition);
        }
        if (newPosition == 0) {
            mPresenter.loadPrev();
        } else if (newPosition == mReaderAdapter.getItemCount() - 1) {
            mPresenter.loadNext();
        }
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            mRecyclerView.scrollToPosition(mRecyclerView.getCurrentPosition() + value - progress);
        }
    }

    @Override
    public void onSingleTap(PhotoDraweeView draweeView, float x, float y) {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        float limitX = point.x / 3.0f;
        float limitY = point.y / 3.0f;
        if (x < limitX) {
            hideToolLayout();
            if (mRecyclerView.getCurrentPosition() == 0) {
                mPresenter.loadPrev();
            } else if (reverse) {
                mRecyclerView.scrollToPosition(mRecyclerView.getCurrentPosition() + 1);
            } else {
                mRecyclerView.scrollToPosition(mRecyclerView.getCurrentPosition() - 1);
            }
        } else if (x > 2 * limitX) {
            hideToolLayout();
            if (mRecyclerView.getCurrentPosition() == mReaderAdapter.getItemCount() - 1) {
                mPresenter.loadNext();
            } else if (reverse) {
                mRecyclerView.scrollToPosition(mRecyclerView.getCurrentPosition() - 1);
            } else {
                mRecyclerView.scrollToPosition(mRecyclerView.getCurrentPosition() + 1);
            }
        } else if (y >= 2 * limitY) {
            switchToolLayout();
        } else if (y >= limitY) {
            draweeView.retry();
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_page_reader;
    }

    @Override
    public void initLoad(int progress, int max, String title) {
        super.initLoad(progress, max, title);
        if (progress != 1) {
            mRecyclerView.scrollToPosition(progress - 1);
        } else {
            setReadProgress(1);
        }
    }

}
