package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.ReaderPresenter;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnSingleTapListener;
import com.hiroshi.cimoc.utils.PreferenceMaster;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/8/6.
 */
public abstract class ReaderActivity extends BaseActivity implements OnSingleTapListener, OnProgressChangeListener {

    @BindView(R.id.reader_chapter_title) TextView mChapterTitle;
    @BindView(R.id.reader_chapter_page) TextView mChapterPage;
    @BindView(R.id.reader_progress_layout) View mProgressLayout;
    @BindView(R.id.reader_back_layout) View mBackLayout;
    @BindView(R.id.reader_loading_layout) View mLoadingLayout;
    @BindView(R.id.reader_seek_bar) DiscreteSeekBar mSeekBar;

    protected ReaderPresenter mPresenter;
    protected int source;
    protected int progress;
    protected int max;

    @Override
    protected void initView() {
        progress = max = 1;
        mSeekBar.setOnProgressChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.setPage(progress);
    }

    @OnClick(R.id.reader_back_btn) void onBackClick() {
        onBackPressed();
    }

    @Override
    protected void initToolbar() {}

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void initPresenter() {
        source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
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
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {}

    public void updateChapterInfo(int max, String title) {
        this.max = max;
        mChapterTitle.setText(title);
    }

    public void hideToolLayout() {
        if (mProgressLayout.isShown()) {
            mProgressLayout.setVisibility(View.INVISIBLE);
            mBackLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void switchToolLayout() {
        if (mProgressLayout.isShown()) {
            mProgressLayout.setVisibility(View.INVISIBLE);
            mBackLayout.setVisibility(View.INVISIBLE);
        } else {
            mSeekBar.setProgress(progress);
            mSeekBar.setMax(max);
            mProgressLayout.setVisibility(View.VISIBLE);
            mBackLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setReadProgress(int progress) {
        this.progress = progress;
        String text = progress + "/" + max;
        mChapterPage.setText(text);
    }

    public void initLoad(int progress, int max, String title) {
        mLoadingLayout.setVisibility(View.INVISIBLE);
        this.max = max;
        mChapterTitle.setText(title);
    }

    public void showToast(int resId) {
        if (!isFinishing()) {
            Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        }
    }

    public abstract void setPrevImage(String[] array);

    public abstract void setNextImage(String[] array);

    public abstract void loadSuccess(boolean isNext);

    private static final String EXTRA_SOURCE = "a";
    private static final String EXTRA_CID = "b";
    private static final String EXTRA_LAST = "c";
    private static final String EXTRA_PAGE = "d";
    private static final String EXTRA_TITLE = "e";
    private static final String EXTRA_PATH = "f";
    private static final String EXTRA_POSITION = "g";

    protected static void putExtras(Intent intent, Comic comic, List<Chapter> list, int position) {
        intent.putExtra(EXTRA_SOURCE, comic.getSource());
        intent.putExtra(EXTRA_CID, comic.getCid());
        intent.putExtra(EXTRA_LAST, comic.getLast());
        intent.putExtra(EXTRA_PAGE, comic.getPage());
        String[][] array = fromList(list);
        intent.putExtra(EXTRA_TITLE, array[0]);
        intent.putExtra(EXTRA_PATH, array[1]);
        intent.putExtra(EXTRA_POSITION, position);
    }

    private static String[][] fromList(List<Chapter> list) {
        int size = list.size();
        String[] title = new String[size];
        String[] path = new String[size];
        for (int i = 0; i != size; ++i) {
            title[i] = list.get(i).getTitle();
            path[i] = list.get(i).getPath();
        }
        return new String[][] { title, path };
    }

    private static Chapter[] fromArray(String[] title, String[] path) {
        int size = title.length;
        Chapter[] array = new Chapter[size];
        for (int i = 0; i != size; ++i) {
            array[i] = new Chapter(title[i], path[i]);
        }
        return array;
    }


    public static Intent createIntent(Context context, Comic comic, List<Chapter> list, int position) {
        int mode = CimocApplication.getPreferences().getInt(PreferenceMaster.PREF_MODE, PreferenceMaster.MODE_HORIZONTAL_PAGE);
        Intent intent;
        if (mode == PreferenceMaster.MODE_HORIZONTAL_PAGE) {
            intent = new Intent(context, PageReaderActivity.class);
        } else {
            intent = new Intent(context, StreamReaderActivity.class);
        }
        putExtras(intent, comic, list, position);
        return intent;
    }

}
