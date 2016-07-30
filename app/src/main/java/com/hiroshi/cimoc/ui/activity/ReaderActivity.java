package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.ReaderPresenter;
import com.hiroshi.cimoc.ui.adapter.PicturePagerAdapter;
import com.hiroshi.cimoc.ui.custom.LimitedViewPager;
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.LinkedList;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class ReaderActivity extends BaseActivity {

    public static final String EXTRA_POSITION = "extra_position";

    @BindView(R.id.reader_view_pager) LimitedViewPager mViewPager;
    @BindView(R.id.reader_chapter_title) TextView mChapterTitle;
    @BindView(R.id.reader_chapter_page) TextView mChapterPage;
    @BindView(R.id.reader_tool_bar) LinearLayout mToolLayout;
    @BindView(R.id.reader_seek_bar) DiscreteSeekBar mSeekBar;

    private PicturePagerAdapter mPagerAdapter;
    private ReaderPresenter mPresenter;

    @Override
    public void onBackPressed() {
        mPresenter.afterRead(mSeekBar.getProgress());
        super.onBackPressed();
    }

    @Override
    protected void initView() {
        mPagerAdapter = new PicturePagerAdapter(new LinkedList<String>(), getLayoutInflater(),
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        int visibility = mToolLayout.isShown() ? View.GONE : View.VISIBLE;
                        mToolLayout.setVisibility(visibility);
                        return true;
                    }
                }, ControllerBuilderFactory.getControllerBuilder(mPresenter.getSource(), this));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                mViewPager.setLimit(mPagerAdapter.getLimitByPosition(position));
                mPresenter.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE && mViewPager.getLimit() == LimitedViewPager.LIMIT_RIGHT) {
                    mPresenter.onPageStateIdle(true);
                } else if (state == ViewPager.SCROLL_STATE_IDLE && mViewPager.getLimit() == LimitedViewPager.LIMIT_LEFT) {
                    mPresenter.onPageStateIdle(false);
                }
            }
        });
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(PicturePagerAdapter.MAX_COUNT / 2 + 1, false);
        mViewPager.setOffscreenPageLimit(4);

        mSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    mViewPager.setCurrentItem(mPresenter.getOffset() + value - 1, false);
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {}
        });
    }

    @Override
    protected void initToolbar() {}

    @Override
    protected void initPresenter() {
        int position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        mPresenter = new ReaderPresenter(this, position);
    }

    @Override
    protected String getDefaultTitle() {
        return null;
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected int getLayoutView() {
        return R.layout.activity_reader;
    }

    public void setPrevImage(String[] array) {
        mPagerAdapter.setPrevImages(array);
    }

    public void setNextImage(String[] array) {
        mPagerAdapter.setNextImages(array);
    }

    public void setNoneLimit() {
        mViewPager.setLimit(LimitedViewPager.LIMIT_NONE);
    }

    public void notifySpecialPage(boolean isFirst, int status) {
        mPagerAdapter.notifySpecialPage(isFirst, status);
    }

    public void hideChapterInfo() {
        mToolLayout.setVisibility(View.GONE);
        mChapterPage.setText(null);
        mChapterTitle.setText(null);
    }

    public void updateChapterInfo(int progress, int max, String title) {
        mSeekBar.setMax(max);
        mSeekBar.setProgress(progress);
        String str = progress + "/" + max;
        mChapterPage.setText(str);
        mChapterTitle.setText(title);
    }

    public void setReadProgress(int progress) {
        mSeekBar.setProgress(progress);
        String pageString = progress + "/" + mSeekBar.getMax();
        mChapterPage.setText(pageString);
    }

    public static Intent createIntent(Context context, int position) {
        Intent intent = new Intent(context, ReaderActivity.class);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

}
