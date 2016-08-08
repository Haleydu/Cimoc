package com.hiroshi.cimoc.ui.activity;

import android.graphics.Point;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.ui.adapter.PicturePageAdapter;
import com.hiroshi.cimoc.ui.custom.LimitedViewPager;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;
import com.hiroshi.cimoc.utils.PreferenceMaster;

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
        super.initView();
        volume = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_VOLUME, false);
        mPageAdapter = new PicturePageAdapter(new LinkedList<String>(), getLayoutInflater(),
                ControllerBuilderFactory.getControllerBuilder(source, this), this);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setCurrentItem(PicturePageAdapter.MAX_COUNT / 2 + 1, false);
        mViewPager.setOffscreenPageLimit(3);
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
        if (position > current && progress == max) {
            mPresenter.onChapterToNext();
        } else if (position < current && progress == 1) {
            mPresenter.onChapterToPrev();
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
                if (mViewPager.getLimit() == LimitedViewPager.LIMIT_LEFT) {
                    mPresenter.loadNext();
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
        if (fromUser) {
            mViewPager.setCurrentItem(mPageAdapter.getCurrent() + value - progress);
        }
    }

    @Override
    public void onSingleTap(PhotoDraweeView draweeView, float x, float y) {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        float limitX = point.x / 3.0f;
        float limitY = point.y / 3.0f;
        if (x < limitX) {
            mViewPager.prevPage();
        } else if (x > 2 * limitX) {
            mViewPager.nextPage();
        } else if (y >= 2 * limitY) {
            switchToolLayout();
        } else if (y >= limitY) {
            draweeView.retry();
        } else if (mPageAdapter.isToLeft()) {
            mPresenter.loadPrev();
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_page_reader;
    }

    @Override
    public void setPrevImage(String[] array) {
        mPageAdapter.setPrevImages(array);
    }

    @Override
    public void setNextImage(String[] array) {
        mPageAdapter.setNextImages(array);
    }

    @Override
    public void loadSuccess(boolean isNext) {
        if (isNext && mViewPager.getLimit() == LimitedViewPager.LIMIT_LEFT ||
                !isNext && mViewPager.getLimit() == LimitedViewPager.LIMIT_RIGHT) {
            mViewPager.setLimit(LimitedViewPager.LIMIT_NONE);
        }
        showToast(R.string.reader_load_success);
    }

    @Override
    public void initLoad(int progress, int max, String title) {
        super.initLoad(progress, max, title);
        if (progress != 1) {
            mViewPager.setCurrentItem(PicturePageAdapter.MAX_COUNT / 2 + progress);
        } else {
            if (mPageAdapter.isToBoth()) {
                mViewPager.setLimit(LimitedViewPager.LIMIT_BOTH);
                mPresenter.loadNext();
            } else {
                mViewPager.setLimit(LimitedViewPager.LIMIT_RIGHT);
            }
            String text = progress + "/" + max;
            mChapterPage.setText(text);
        }
    }

}
