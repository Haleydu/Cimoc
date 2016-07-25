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
import com.hiroshi.cimoc.utils.ControllerBuilderFactory;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.LinkedList;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class ReaderActivity extends BaseActivity {

    public static final String EXTRA_POSITION = "extra_position";

    @BindView(R.id.reader_view_pager) ViewPager mViewPager;
    @BindView(R.id.reader_chapter_title) TextView mChapterTitle;
    @BindView(R.id.reader_chapter_page) TextView mChapterPage;
    @BindView(R.id.reader_tool_bar) LinearLayout mToolLayout;
    @BindView(R.id.reader_seek_bar) DiscreteSeekBar mSeekBar;

    private PicturePagerAdapter mPagerAdapter;
    private ReaderPresenter mPresenter;

    @Override
    public void onBackPressed() {
        mPresenter.setPage(mSeekBar.getProgress());
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
                }, ControllerBuilderFactory.getControllerBuilder(mPresenter.getSource()));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                mPresenter.onPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        mViewPager.setOffscreenPageLimit(6);
        mViewPager.setAdapter(mPagerAdapter);

        mSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    mViewPager.setCurrentItem(mPresenter.getOffset() + value, false);
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
        int position = getIntent().getIntExtra(EXTRA_POSITION, 1);
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

    public int getCount() {
        return mPagerAdapter.getCount();
    }

    public void setInitImage(String[] array, boolean absence) {
        mPagerAdapter.setAbsence(absence);
        mPagerAdapter.setNextImages(array);
        mViewPager.setCurrentItem(1, false);
    }

    public void setPrevImage(String[] array, boolean absence) {
        mPagerAdapter.setAbsence(absence);
        mPagerAdapter.setPrevImages(array);
        mViewPager.setCurrentItem(array.length, false);
    }

    public void setNextImage(String[] array) {
        mPagerAdapter.setNextImages(array);
    }

    public void clearInformation() {
        mChapterTitle.setText(null);
        mChapterPage.setText(null);
    }

    public void setReadMax(int max, int progress) {
        mSeekBar.setMax(max);
        mSeekBar.setProgress(progress);
        String pageString = progress + "/" + max;
        mChapterPage.setText(pageString);
    }

    public void setReadProgress(int progress) {
        mSeekBar.setProgress(progress);
        String pageString = progress + "/" + mSeekBar.getMax();
        mChapterPage.setText(pageString);
    }

    public void setTitle(String title) {
        mChapterTitle.setText(title);
    }

    public static Intent createIntent(Context context, int position) {
        Intent intent = new Intent(context, ReaderActivity.class);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

}
