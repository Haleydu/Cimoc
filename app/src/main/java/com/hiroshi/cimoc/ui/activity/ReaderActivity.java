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
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/7/7.
 */
public class ReaderActivity extends BaseActivity {

    public static final String EXTRA_CHAPTERS = "extra_chapters";
    public static final String EXTRA_POSITION = "extra_position";
    public static final String EXTRA_SOURCE = "extra_source";

    @BindView(R.id.reader_view_pager) ViewPager mViewPager;
    @BindView(R.id.reader_chapter_title) TextView mChapterTitle;
    @BindView(R.id.reader_chapter_page) TextView mChapterPage;

    private PicturePagerAdapter mPagerAdapter;
    private ReaderPresenter mPresenter;
    private ArrayList<Chapter> mChapterList;
    private List<Information> mInformation;

    private int prev;
    private int next;

    @Override
    protected void initView() {
        mPagerAdapter = new PicturePagerAdapter(new LinkedList<String>(), getLayoutInflater());
        mInformation = new LinkedList<>();
        mViewPager.addOnPageChangeListener(mPresenter.getPageChangeListener());
        mViewPager.setOffscreenPageLimit(8);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(0);

        int pos = getIntent().getIntExtra(EXTRA_POSITION, 1);
        prev = pos + 1;
        next = pos;
        mChapterList = getIntent().getParcelableArrayListExtra(EXTRA_CHAPTERS);
        mPresenter.initPicture(mChapterList.get(pos).getPath());
    }

    @Override
    protected void initToolbar() {}

    @Override
    protected void initPresenter() {
        int source =  getIntent().getIntExtra(EXTRA_SOURCE, 0);
        mPresenter = new ReaderPresenter(this, source);
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

    public String getPrevPath() {
        if (prev < mChapterList.size()) {
            return mChapterList.get(prev).getPath();
        }
        return null;
    }

    public String getNextPath() {
        if (next > 0) {
            return mChapterList.get(next).getPath();
        }
        return null;
    }

    public int getCount() {
        return mPagerAdapter.getCount();
    }

    public void setPrevImage(String[] array) {
        mPagerAdapter.setOffset(array.length);
        mPagerAdapter.setPrevImages(array);
        mInformation.add(0, new Information(mChapterList.get(prev).getTitle(), array.length));
        ++prev;
    }

    public void setNextImage(String[] array) {
        mPagerAdapter.setOffset(0);
        mPagerAdapter.setNextImages(array);
        mInformation.add(new Information(mChapterList.get(next).getTitle(), array.length));
        --next;
    }

    public void setInformation(int position) {
        int count = 0;
        for (Information info : mInformation) {
            if (count + info.page > position) {
                int current = position - count + 1;
                mChapterTitle.setText(info.title);
                String page = current + "/" + info.page;
                mChapterPage.setText(page);
                break;
            }
            count += info.page;
        }
    }

    public static Intent createIntent(Context context, ArrayList<Chapter> chapters, int position, int source) {
        Intent intent = new Intent(context, ReaderActivity.class);
        intent.putExtra(EXTRA_CHAPTERS, chapters);
        intent.putExtra(EXTRA_POSITION, position);
        intent.putExtra(EXTRA_SOURCE, source);
        return intent;
    }

    private static class Information {

        String title;
        int page;

        Information(String title, int page) {
            this.title = title;
            this.page = page;
        }

    }

}
