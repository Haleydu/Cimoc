package com.hiroshi.cimoc.ui.activity;

import android.graphics.Point;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.PreferenceMaster;
import com.hiroshi.cimoc.ui.adapter.PicturePageAdapter;
import com.hiroshi.cimoc.ui.custom.LimitedViewPager;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.LinkedList;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PageReaderActivity extends ReaderActivity implements OnPageChangeListener {

    @BindView(R.id.reader_view_pager) LimitedViewPager mViewPager;

    private PicturePageAdapter mPageAdapter;

    private boolean volume;
    private boolean reverse;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (volume) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    mViewPager.prevPage();
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    mViewPager.nextPage();
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void initView() {
        if (shouldCreate()) {
            super.initView();
            reverse = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_REVERSE, false);
            mSeekBar.setReverse(reverse);
            volume = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_VOLUME, false);
            mPageAdapter = new PicturePageAdapter(new LinkedList<String>(), getLayoutInflater(),
                    ControllerBuilderFactory.getControllerBuilder(source, this), this);
            int current = reverse ? PicturePageAdapter.MAX_COUNT / 2 : PicturePageAdapter.MAX_COUNT / 2 + 1;
            mPageAdapter.setCurrent(current);
            mViewPager.addOnPageChangeListener(this);
            mViewPager.setAdapter(mPageAdapter);
            mViewPager.setCurrentItem(current, false);
            mViewPager.setOffscreenPageLimit(3);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageSelected(int position) {
        int current = mPageAdapter.getCurrent();
        if (position == current) {
            return;
        }
        mPageAdapter.setCurrent(position);
        if (mPageAdapter.isToLeft()) {
            mViewPager.setLimit(LimitedViewPager.LIMIT_RIGHT);
        } else if (mPageAdapter.isToRight()) {
            mViewPager.setLimit(LimitedViewPager.LIMIT_LEFT);
        } else {
            mViewPager.setLimit(LimitedViewPager.LIMIT_NONE);
        }
        if (progress == max && (position < current && reverse || position > current && !reverse)) {
            mPresenter.toNextChapter();
        } else if (progress == 1 && (position > current && reverse || position < current && !reverse)) {
            mPresenter.toPrevChapter();
        } else if (reverse) {
            setReadProgress(progress + current - position);
        } else {
            setReadProgress(progress + position - current);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        switch (state) {
            case ViewPager.SCROLL_STATE_DRAGGING:
                hideToolLayout();
                break;
            case ViewPager.SCROLL_STATE_IDLE:
                if (reverse && mPageAdapter.isToLeft() || !reverse && mPageAdapter.isToRight()) {
                    mPresenter.loadNext();
                } else if (reverse && mPageAdapter.isToRight() || !reverse && mPageAdapter.isToLeft()) {
                    mPresenter.loadPrev();
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            int offset = reverse ? progress - value : value - progress;
            mViewPager.setCurrentItem(mPageAdapter.getCurrent() + offset);
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
            mViewPager.prevPage();
        } else if (x > 2 * limitX) {
            hideToolLayout();
            mViewPager.nextPage();
        } else if (y >= 2 * limitY) {
            switchToolLayout();
        } else if (y >= limitY) {
            draweeView.retry();
        } else if (!reverse && mPageAdapter.isToLeft() || reverse && mPageAdapter.isToRight()) {
            mPresenter.loadPrev();
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_page_reader;
    }

    @Override
    public void setPrevImage(String[] array) {
        if (reverse) {
            int size = array.length;
            for (int i = 0; i != size / 2; ++i) {
                String temp = array[i];
                array[i] = array[size - i - 1];
                array[size - i - 1] = temp;
            }
            mPageAdapter.setNextImages(array);
        } else {
            mPageAdapter.setPrevImages(array);
        }
    }

    @Override
    public void setNextImage(String[] array) {
        if (reverse) {
            int size = array.length;
            for (int i = 0; i != size / 2; ++i) {
                String temp = array[i];
                array[i] = array[size - i - 1];
                array[size - i - 1] = temp;
            }
            mPageAdapter.setPrevImages(array);
        } else {
            mPageAdapter.setNextImages(array);
        }
    }

    @Override
    public void loadSuccess(boolean isNext) {
        if (!reverse && (isNext && mViewPager.limitLeft() || !isNext && mViewPager.limitRight())
                || reverse && (isNext && mViewPager.limitRight() || !isNext && mViewPager.limitLeft())) {
            mViewPager.setLimit(LimitedViewPager.LIMIT_NONE);
        }
        showToast(R.string.reader_load_success);
    }

    @Override
    public void initLoad(int progress, int max, String title) {
        super.initLoad(progress, max, title);
        if (progress != 1) {
            if (reverse) {
                mViewPager.setCurrentItem(PicturePageAdapter.MAX_COUNT / 2 - progress + 1);
            } else {
                mViewPager.setCurrentItem(PicturePageAdapter.MAX_COUNT / 2 + progress);
            }
        } else {
            if (max == 1) {
                mViewPager.setLimit(LimitedViewPager.LIMIT_BOTH);
                mPresenter.loadNext();
            } else if (reverse) {
                mViewPager.setLimit(LimitedViewPager.LIMIT_LEFT);
            } else {
                mViewPager.setLimit(LimitedViewPager.LIMIT_RIGHT);
            }
            String text = progress + "/" + max;
            mChapterPage.setText(text);
        }
    }

}
