package com.hiroshi.cimoc.ui.activity;

import android.graphics.Point;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.PreferenceMaster;
import com.hiroshi.cimoc.ui.adapter.PictureStreamAdapter;
import com.hiroshi.cimoc.ui.custom.PreCacheLayoutManager;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.Arrays;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/8/5.
 */
public class StreamReaderActivity extends ReaderActivity {

    @BindView(R.id.reader_recycler_view) RecyclerView mRecyclerView;

    private PictureStreamAdapter mStreamAdapter;
    private PreCacheLayoutManager mLayoutManager;

    private int position = 0;

    @Override
    protected void initView() {
        if (shouldCreate()) {
            super.initView();
            boolean split = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_SPLIT, false);
            mLayoutManager = new PreCacheLayoutManager(this);
            mLayoutManager.setExtraSpace(8);
            mStreamAdapter = new PictureStreamAdapter(this, ControllerBuilderFactory.getControllerBuilder(source, this), this, split);
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mStreamAdapter);
            mRecyclerView.addItemDecoration(mStreamAdapter.getItemDecoration());
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
                            if (item == mStreamAdapter.getItemCount() - 1) {
                                mPresenter.loadNext();
                            }
                            break;
                    }
                }

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    int item = mLayoutManager.findFirstVisibleItemPosition();
                    if (item != position) {
                        position = item;
                        if (dy > 0) {
                            if (progress == max) {
                                mPresenter.onChapterToNext();
                            } else {
                                setReadProgress(progress + 1);
                            }
                        } else if (dy < 0) {
                            if (progress == 1) {
                                mPresenter.onChapterToPrev();
                            } else {
                                setReadProgress(progress - 1);
                            }
                        } else {
                            setReadProgress(progress);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            int offset = value - progress;
            progress = value;
            mLayoutManager.scrollToPositionWithOffset(position + offset, 0);
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
    protected int getLayoutRes() {
        return R.layout.activity_stream_reader;
    }

    @Override
    public void setPrevImage(String[] array) {
        mStreamAdapter.addAll(0, Arrays.asList(array));
    }

    @Override
    public void setNextImage(String[] array) {
        mStreamAdapter.addAll(Arrays.asList(array));
    }

    @Override
    public void loadSuccess(boolean isNext) {
        showToast(R.string.reader_load_success);
    }

    @Override
    public void initLoad(int progress, int max, String title) {
        super.initLoad(progress, max, title);
        this.progress = progress;
        if (progress != 1) {
            mRecyclerView.scrollToPosition(progress - 1);
        } else {
            String text = progress + "/" + max;
            mChapterPage.setText(text);
        }
    }

}
