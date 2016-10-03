package com.hiroshi.cimoc.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.fresco.ControllerBuilderSupplierFactory;
import com.hiroshi.cimoc.fresco.ImagePipelineFactoryBuilder;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.presenter.ReaderPresenter;
import com.hiroshi.cimoc.ui.adapter.ReaderAdapter;
import com.hiroshi.cimoc.ui.adapter.ReaderAdapter.OnLazyLoadListener;
import com.hiroshi.cimoc.ui.custom.PreCacheLayoutManager;
import com.hiroshi.cimoc.ui.custom.ReverseSeekBar;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnLongPressListener;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnSingleTapListener;
import com.hiroshi.cimoc.ui.view.ReaderView;
import com.hiroshi.cimoc.utils.FileUtils;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/8/6.
 */
public abstract class ReaderActivity extends BaseActivity implements OnSingleTapListener,
        OnProgressChangeListener, OnLongPressListener, OnLazyLoadListener, ReaderView {

    @BindView(R.id.reader_chapter_title) TextView mChapterTitle;
    @BindView(R.id.reader_chapter_page) TextView mChapterPage;
    @BindView(R.id.reader_battery) TextView mBatteryText;
    @BindView(R.id.reader_progress_layout) View mProgressLayout;
    @BindView(R.id.reader_back_layout) View mBackLayout;
    @BindView(R.id.reader_info_layout) View mInfoLayout;
    @BindView(R.id.reader_seek_bar) ReverseSeekBar mSeekBar;

    @BindView(R.id.reader_recycler_view) RecyclerView mRecyclerView;

    protected PreCacheLayoutManager mLayoutManager;
    protected ReaderAdapter mReaderAdapter;
    protected ImagePipelineFactory mImagePipelineFactory;

    protected ReaderPresenter mPresenter;
    protected int progress = 1;
    protected int max = 1;
    protected boolean reverse = false;
    protected int turn;

    private boolean hide;

    @Override
    protected void initTheme() {
        super.initTheme();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_BRIGHT, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    protected void initProgressBar() {}

    @Override
    protected void initView() {
        hide = CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_HIDE, false);
        if (hide) {
            mInfoLayout.setVisibility(View.INVISIBLE);
        }
        turn = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_READER_TURN, PreferenceManager.READER_TURN_LTR);
        reverse = turn == PreferenceManager.READER_TURN_RTL;
        mSeekBar.setReverse(reverse);
        mSeekBar.setOnProgressChangeListener(this);
        initLayoutManager();
        initReaderAdapter();
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mReaderAdapter);
    }

    private void initReaderAdapter() {
        List<ImageUrl> list = new LinkedList<>();
        list.add(new ImageUrl(0, null, true));
        mReaderAdapter = new ReaderAdapter(this, list);
        mReaderAdapter.setSingleTapListener(this);
        mReaderAdapter.setLazyLoadListener(this);
        if (CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_PICTURE, false)) {
            mReaderAdapter.setLongPressListener(this);
        }
        mReaderAdapter.setFitMode(turn == PreferenceManager.READER_TURN_ATB ? ReaderAdapter.FIT_WIDTH : ReaderAdapter.FIT_HEIGHT);
        mReaderAdapter.setAutoSplit(CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_SPLIT, false));
    }

    private void initLayoutManager() {
        mLayoutManager = new PreCacheLayoutManager(this);
        mLayoutManager.setOrientation(turn == PreferenceManager.READER_TURN_ATB ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL);
        mLayoutManager.setExtraSpace(3);
        mLayoutManager.setReverseLayout(reverse);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Parcelable[] array = getIntent().getParcelableArrayExtra(EXTRA_CHAPTER);
        Chapter[] chapter = new Chapter[array.length];
        for (int i = 0; i != array.length; ++i) {
            chapter[i] = (Chapter) array[i];
        }
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        mPresenter.loadInit(id, chapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(batteryReceiver,  new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPresenter != null) {
            mPresenter.updateComic(progress);
        }
        unregisterReceiver(batteryReceiver);
    }

    @Override
    protected void onDestroy() {
        if (mImagePipelineFactory != null) {
            mImagePipelineFactory.getImagePipeline().clearMemoryCaches();
        }
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {}

    @OnClick(R.id.reader_back_btn) void onBackClick() {
        onBackPressed();
    }

    @Override
    protected void initToolbar() {}

    @Override
    protected void initPresenter() {
        mPresenter = new ReaderPresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void onStartTrackingTouch(DiscreteSeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(DiscreteSeekBar seekBar) {}

    @Override
    public void onLoad(ImageUrl imageUrl) {
        mPresenter.lazyLoad(imageUrl);
    }

    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                String text = (level * 100 / scale) + "%";
                mBatteryText.setText(text);
            }
        }
    };

    protected void hideToolLayout() {
        if (mProgressLayout.isShown()) {
            mProgressLayout.setVisibility(View.INVISIBLE);
            mBackLayout.setVisibility(View.INVISIBLE);
            if (hide) {
                mInfoLayout.setVisibility(View.INVISIBLE);
            }
        }
    }

    protected void switchToolLayout() {
        if (mProgressLayout.isShown()) {
            hideToolLayout();
        } else {
            if (mSeekBar.getMax() != max) {
                mSeekBar.setMax(max);
                mSeekBar.setProgress(max);
            }
            mSeekBar.setProgress(progress);
            mProgressLayout.setVisibility(View.VISIBLE);
            mBackLayout.setVisibility(View.VISIBLE);
            if (hide) {
                mInfoLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void updateProgress() {
        mChapterPage.setText(StringUtils.getProgress(progress, max));
    }

    @Override
    public void onParseError() {
        HintUtils.showToast(this, R.string.common_parse_error);
    }

    @Override
    public void onNetworkError() {
        HintUtils.showToast(this, R.string.common_network_error);
    }

    @Override
    public void onNextLoadSuccess(List<ImageUrl> list) {
        mReaderAdapter.addAll(list);
        HintUtils.showToast(this, R.string.reader_load_success);
    }

    @Override
    public void onPrevLoadSuccess(List<ImageUrl> list) {
        mReaderAdapter.addAll(0, list);
        HintUtils.showToast(this, R.string.reader_load_success);
    }

    @Override
    public void onInitLoadSuccess(List<ImageUrl> list, int progress, int source) {
        mImagePipelineFactory = ImagePipelineFactoryBuilder.build(this, source);
        mReaderAdapter.setControllerSupplier(ControllerBuilderSupplierFactory.get(this, mImagePipelineFactory));
        mReaderAdapter.setData(list);
        if (progress != 1) {
            mRecyclerView.scrollToPosition(progress - 1);
        }
        updateProgress();
    }

    @Override
    public void onChapterChange(Chapter chapter) {
        max = chapter.getCount();
        mChapterTitle.setText(chapter.getTitle());
    }

    @Override
    public void onImageLoadSuccess(int id, String url) {
        mReaderAdapter.update(id, url);
    }

    @Override
    public void onImageLoadFail(int id) {
        mReaderAdapter.update(id, null);
    }

    @Override
    public void onPictureSaveSuccess() {
        HintUtils.showToast(this, R.string.reader_picture_save_success);
    }

    @Override
    public void onPictureSaveFail() {
        HintUtils.showToast(this, R.string.reader_picture_save_fail);
    }

    @Override
    public void onPrevLoading() {
        HintUtils.showToast(this, R.string.reader_load_prev);
    }

    @Override
    public void onPrevLoadNone() {
        HintUtils.showToast(this, R.string.reader_prev_none);
    }

    @Override
    public void onNextLoading() {
        HintUtils.showToast(this, R.string.reader_load_next);
    }

    @Override
    public void onNextLoadNone() {
        HintUtils.showToast(this, R.string.reader_next_none);
    }

    protected void savePicture(int position) {
        String url = mReaderAdapter.getItem(position).getUrl();
        try {
            String suffix = StringUtils.getSplit(url, "\\.", -1);
            if (url.startsWith("file")) {
                InputStream inputStream = FileUtils.getInputStream(url.replace("file://", "."));
                mPresenter.savePicture(inputStream, suffix);
            } else {
                BinaryResource resource = mImagePipelineFactory.getMainFileCache().getResource(new SimpleCacheKey(url));
                if (resource != null) {
                    mPresenter.savePicture(resource.openStream(), suffix);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            HintUtils.showToast(this, R.string.reader_picture_save_fail);
        }
    }

    private static final String EXTRA_ID = "a";
    private static final String EXTRA_CHAPTER = "b";

    public static Intent createIntent(Context context, long id, List<Chapter> list) {
        Intent intent = getIntent(context);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_CHAPTER, list.toArray(new Chapter[list.size()]));
        return intent;
    }

    private static Intent getIntent(Context context) {
        int mode = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        int orientation = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_READER_ORIENTATION, PreferenceManager.READER_ORIENTATION_PORTRAIT);
        switch (mode) {
            default:
            case PreferenceManager.READER_MODE_PAGE:
                if (orientation == PreferenceManager.READER_ORIENTATION_PORTRAIT) {
                    return new Intent(context, PageReaderActivity.class);
                } else {
                    return new Intent(context, LandscapePageReaderActivity.class);
                }
            case PreferenceManager.READER_MODE_STREAM:
                if (orientation == PreferenceManager.READER_ORIENTATION_PORTRAIT) {
                    return new Intent(context, StreamReaderActivity.class);
                } else {
                    return new Intent(context, LandscapeStreamReaderActivity.class);
                }
        }
    }

}
