package com.hiroshi.cimoc.ui.activity;

import android.graphics.Point;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.ui.adapter.ReaderAdapter;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;

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
        mLayoutManager.setExtraSpace(4);
        mReaderAdapter.setPictureMode(ReaderAdapter.MODE_STREAM);
        mReaderAdapter.setAutoSplit(CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_SPLIT, false));
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mReaderAdapter);
        if (!CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_BLANK, false)) {
            mRecyclerView.addItemDecoration(mReaderAdapter.getItemDecoration());
        }
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        hideToolLayout();
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
                int item = mLayoutManager.findFirstVisibleItemPosition();
                if (item != position) {
                    if (dy > 0 && progress == max) {
                        mPresenter.toNextChapter();
                    } else if (dy < 0 && progress == 1) {
                        mPresenter.toPrevChapter();
                    }
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
    public void onSingleTap(PhotoDraweeView draweeView, float x, float y) {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        float limitY = point.y / 3.0f;
        if (mRecyclerView.getChildAdapterPosition(draweeView) == 0 && y < limitY) {
            mPresenter.loadPrev();
        } else if (!draweeView.retry()) {
            switchToolLayout();
        }
    }

    @Override
    public void onLongPress(PhotoDraweeView draweeView) {
        int position = mRecyclerView.getChildAdapterPosition(draweeView);
        savePicture(position);
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
