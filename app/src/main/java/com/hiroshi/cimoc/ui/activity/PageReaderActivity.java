package com.hiroshi.cimoc.ui.activity;

import android.graphics.Point;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.ui.custom.rvp.RecyclerViewPager;
import com.hiroshi.cimoc.ui.custom.rvp.RecyclerViewPager.OnPageChangedListener;
import com.hiroshi.cimoc.utils.HintUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PageReaderActivity extends ReaderActivity implements OnPageChangedListener {

    private boolean volume;
    private boolean reverse;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (volume) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    mRecyclerView.scrollToPosition(((RecyclerViewPager) mRecyclerView).getCurrentPosition() - 1);
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    mRecyclerView.scrollToPosition(((RecyclerViewPager) mRecyclerView).getCurrentPosition() + 1);
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void initView() {
        super.initView();
        int offset = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_TRIGGER, 5);
        reverse = CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_REVERSE, false);
        volume = CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_VOLUME, false);
        mSeekBar.setReverse(reverse);
        mLayoutManager.setExtraSpace(3);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mLayoutManager.setReverseLayout(reverse);
        mReaderAdapter.setAutoSplit(false);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mReaderAdapter);
        ((RecyclerViewPager) mRecyclerView).setTriggerOffset(0.01f * offset);
        ((RecyclerViewPager) mRecyclerView).addOnPageChangedListener(this);
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
        if (oldPosition == newPosition) {
            return;
        }

        if (newPosition == 0) {
            mPresenter.loadPrev();
        } else if (newPosition == mReaderAdapter.getItemCount() - 1) {
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
        ((RecyclerViewPager) mRecyclerView).refreshBeforePosition(list.size());
        HintUtils.showToast(this, R.string.reader_load_success);
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            mRecyclerView.scrollToPosition(((RecyclerViewPager) mRecyclerView).getCurrentPosition() + value - progress);
        }
    }

    @Override
    public void onSingleTap(PhotoDraweeView draweeView, float x, float y) {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        float limitX = point.x / 3.0f;
        float limitY = point.y / 3.0f;
        if (x < limitX) {
            int position = ((RecyclerViewPager) mRecyclerView).getCurrentPosition();
            hideToolLayout();
            if (position == 0) {
                mPresenter.loadPrev();
            } else if (reverse) {
                mRecyclerView.scrollToPosition(position + 1);
            } else {
                mRecyclerView.scrollToPosition(position - 1);
            }
        } else if (x > 2 * limitX) {
            int position = ((RecyclerViewPager) mRecyclerView).getCurrentPosition();
            hideToolLayout();
            if (position == mReaderAdapter.getItemCount() - 1) {
                mPresenter.loadNext();
            } else if (reverse) {
                mRecyclerView.scrollToPosition(position - 1);
            } else {
                mRecyclerView.scrollToPosition(position + 1);
            }
        } else if (y >= 2 * limitY) {
            switchToolLayout();
        } else if (y >= limitY) {
            draweeView.retry();
        }
    }

    @Override
    public void onLongPress(PhotoDraweeView draweeView) {
        int position = ((RecyclerViewPager) mRecyclerView).getCurrentPosition();
        savePicture(position);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_page_reader;
    }

}
