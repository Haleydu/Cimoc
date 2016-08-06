package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.ReaderPresenter;
import com.hiroshi.cimoc.ui.adapter.PicturePageAdapter;
import com.hiroshi.cimoc.ui.custom.LimitedViewPager;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;
import com.hiroshi.cimoc.utils.PreferenceMaster;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class PageReaderActivity extends ReaderActivity {

    @BindView(R.id.reader_view_pager) LimitedViewPager mViewPager;

    private PicturePageAdapter mPageAdapter;
    private ReaderPresenter mPresenter;

    private boolean volume;

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.setPage(progress);
    }

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
        volume = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_VOLUME, false);
        progress = max = 1;

        mPageAdapter = new PicturePageAdapter(new LinkedList<String>(), getLayoutInflater(),
                new PhotoDraweeViewController.OnSingleTapListener() {
                    @Override
                    public void onSingleTap(View view, float x, float y) {
                        Point point = new Point();
                        getWindowManager().getDefaultDisplay().getSize(point);
                        float limitX = point.x / 3.0f;
                        float limitY = point.y / 3.0f;
                        if (x < limitX) {
                            mViewPager.prevPage();
                        } else if (x > 2 * limitX) {
                            mViewPager.nextPage();
                        } else if (y >= 2 * limitY) {
                            if (mToolLayout.isShown()) {
                                mToolLayout.setVisibility(View.VISIBLE);
                            } else {
                                mSeekBar.setProgress(progress);
                                mSeekBar.setMax(max);
                                mToolLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }, ControllerBuilderFactory.getControllerBuilder(getIntent().getIntExtra(EXTRA_SOURCE, -1), this));

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                        if (mToolLayout.isShown()) {
                            mToolLayout.setVisibility(View.INVISIBLE);
                        }
                        break;
                    case ViewPager.SCROLL_STATE_IDLE:
                        if (mViewPager.getLimit() == LimitedViewPager.LIMIT_RIGHT) {
                            mPresenter.loadPrev();
                        } else if (mViewPager.getLimit() == LimitedViewPager.LIMIT_LEFT) {
                            mPresenter.loadNext();
                        }
                        break;
                }
            }
        });
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setCurrentItem(PicturePageAdapter.MAX_COUNT / 2 + 1, false);
        mViewPager.setOffscreenPageLimit(3);

        mSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    mViewPager.setCurrentItem(mPageAdapter.getCurrent() + value - progress, false);
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {}
        });
    }

    @Override
    protected void initPresenter() {
        int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        String cid = getIntent().getStringExtra(EXTRA_CID);
        String last = getIntent().getStringExtra(EXTRA_LAST);
        int page = getIntent().getIntExtra(EXTRA_PAGE, -1);
        String[] title = getIntent().getStringArrayExtra(EXTRA_TITLE);
        String[] path = getIntent().getStringArrayExtra(EXTRA_PATH);
        Chapter[] array = fromArray(title, path);
        int position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        mPresenter = new ReaderPresenter(this, source, cid, last, page, array, position);
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_reader;
    }

    public void setPrevImage(String[] array) {
        mPageAdapter.setPrevImages(array);
    }

    public void setNextImage(String[] array) {
        mPageAdapter.setNextImages(array);
    }

    public void loadSuccess(boolean isNext) {
        if (isNext && mViewPager.getLimit() == LimitedViewPager.LIMIT_LEFT ||
                !isNext && mViewPager.getLimit() == LimitedViewPager.LIMIT_RIGHT) {
            mViewPager.setLimit(LimitedViewPager.LIMIT_NONE);
        }
    }

    public void initLoad(int progress, int max, String title) {
        this.max = max;
        mChapterTitle.setText(title);
        if (progress != 1) {
            mViewPager.setCurrentItem(PicturePageAdapter.MAX_COUNT / 2 + progress);
        } else {
            mViewPager.setLimit(LimitedViewPager.LIMIT_RIGHT);
            String text = progress + "/" + max;
            mChapterPage.setText(text);
        }
    }

    public void updateChapterInfo(int count, String title) {
        max = count;
        mChapterTitle.setText(title);
    }

    public void setReadProgress(int progress) {
        this.progress = progress;
        String text = progress + "/" + max;
        mChapterPage.setText(text);
    }

    public static Intent createIntent(Context context, Comic comic, List<Chapter> list, int position) {
        Intent intent = new Intent(context, PageReaderActivity.class);
        putExtras(intent, comic, list, position);
        return intent;
    }

}
