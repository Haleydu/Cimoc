package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.ReaderPresenter;
import com.hiroshi.cimoc.ui.adapter.PicturePagerAdapter;

import java.util.ArrayList;
import java.util.LinkedList;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class ReaderActivity extends BaseActivity {

    public static final String EXTRA_CHAPTERS = "extra_chapters";
    public static final String EXTRA_POSITION = "extra_position";

    @BindView(R.id.reader_view_pager) ViewPager mViewPager;
    @BindView(R.id.reader_chapter_title) TextView mChapterTitle;
    @BindView(R.id.reader_chapter_page) TextView mChapterPage;

    private PicturePagerAdapter mPagerAdapter;
    private ReaderPresenter mPresenter;

    @Override
    protected void initView() {
        mPagerAdapter = new PicturePagerAdapter(new LinkedList<String>(), getLayoutInflater());
        mViewPager.addOnPageChangeListener(mPresenter.getPageChangeListener());
        mViewPager.setOffscreenPageLimit(8);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(0);

        mPresenter.initPicture();
    }

    @Override
    protected void initToolbar() {}

    @Override
    protected void initPresenter() {
        int position = getIntent().getIntExtra(EXTRA_POSITION, 1);
        ArrayList<Chapter> list = getIntent().getParcelableArrayListExtra(EXTRA_CHAPTERS);
        mPresenter = new ReaderPresenter(this, list, position);
    }

    @Override
    protected void onDestroy() {
        mPresenter.setLastPage();
        super.onDestroy();
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

    public void setInformation(String title, int cur, int page) {
        mChapterTitle.setText(title);
        String str = cur + "/" + page;
        mChapterPage.setText(str);
    }

    public static Intent createIntent(Context context, ArrayList<Chapter> chapters, int position) {
        Intent intent = new Intent(context, ReaderActivity.class);
        intent.putExtra(EXTRA_CHAPTERS, chapters);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }



}
