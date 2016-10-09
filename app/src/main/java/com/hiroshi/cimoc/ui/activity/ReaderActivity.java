package com.hiroshi.cimoc.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
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
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeView;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnLongPressListener;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnSingleTapListener;
import com.hiroshi.cimoc.ui.view.ReaderView;
import com.hiroshi.cimoc.utils.EventUtils;
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
    @BindView(R.id.reader_loading) TextView mLoadingText;

    @BindView(R.id.reader_recycler_view) RecyclerView mRecyclerView;

    protected PreCacheLayoutManager mLayoutManager;
    protected ReaderAdapter mReaderAdapter;
    protected ImagePipelineFactory mImagePipelineFactory;

    protected ReaderPresenter mPresenter;
    protected int progress = 1;
    protected int max = 1;
    protected int turn;
    protected boolean isPortrait;

    private boolean hide;
    private int[] mClickArray;
    private int[] mLongClickArray;

    @Override
    protected void initTheme() {
        super.initTheme();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_BRIGHT, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        isPortrait = CimocApplication.getPreferences()
                .getInt(PreferenceManager.PREF_READER_ORIENTATION, PreferenceManager.READER_ORIENTATION_PORTRAIT)
                == PreferenceManager.READER_ORIENTATION_PORTRAIT;
        int value = isPortrait ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setRequestedOrientation(value);
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
        mSeekBar.setReverse(turn == PreferenceManager.READER_TURN_RTL);
        mSeekBar.setOnProgressChangeListener(this);
        initLayoutManager();
        initReaderAdapter();
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mReaderAdapter);
    }

    private void initReaderAdapter() {
        mReaderAdapter = new ReaderAdapter(this, new LinkedList<ImageUrl>());
        mReaderAdapter.setSingleTapListener(this);
        mReaderAdapter.setLongPressListener(this);
        mReaderAdapter.setLazyLoadListener(this);
        mReaderAdapter.setFitMode(turn == PreferenceManager.READER_TURN_ATB ? ReaderAdapter.FIT_WIDTH : ReaderAdapter.FIT_HEIGHT);
        mReaderAdapter.setAutoSplit(CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_SPLIT, false));
    }

    private void initLayoutManager() {
        mLayoutManager = new PreCacheLayoutManager(this);
        mLayoutManager.setOrientation(turn == PreferenceManager.READER_TURN_ATB ? LinearLayoutManager.VERTICAL : LinearLayoutManager.HORIZONTAL);
        mLayoutManager.setReverseLayout(turn == PreferenceManager.READER_TURN_RTL);
        mLayoutManager.setExtraSpace(2);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mClickArray = EventUtils.getClickEventChoice(CimocApplication.getPreferences());
        mLongClickArray = EventUtils.getLongClickEventChoice(CimocApplication.getPreferences());
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
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
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
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
        if (mImagePipelineFactory != null) {
            mImagePipelineFactory.getImagePipeline().clearMemoryCaches();
            mImagePipelineFactory = null;
        }
        mReaderAdapter = null;
    }

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

    protected void hideControl() {
        if (mProgressLayout.isShown()) {
            mProgressLayout.setVisibility(View.INVISIBLE);
            mBackLayout.setVisibility(View.INVISIBLE);
            if (hide) {
                mInfoLayout.setVisibility(View.INVISIBLE);
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
        mReaderAdapter.addAll(list);
        if (progress != 1) {
            mRecyclerView.scrollToPosition(progress - 1);
        }
        mLoadingText.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
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

    /**
     *  Click Event Function
     */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mReaderAdapter.getItemCount() != 0) {
            int value = EventUtils.EVENT_NULL;
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:
                    value = mClickArray[5];
                    break;
                case KeyEvent.KEYCODE_VOLUME_DOWN:
                    value = mClickArray[6];
                    break;
            }
            if (value != EventUtils.EVENT_NULL) {
                doClickEvent(value);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onSingleTap(PhotoDraweeView draweeView, float x, float y) {
        doClickEvent(getValue(draweeView, x, y, false));
    }

    @Override
    public void onLongPress(PhotoDraweeView draweeView, float x, float y) {
        doClickEvent(getValue(draweeView, x, y, true));
    }

    private int getValue(PhotoDraweeView draweeView, float x, float y, boolean isLong) {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        float limitX = point.x / 3.0f;
        float limitY = point.y / 3.0f;
        if (x < limitX) {
            return isLong ? mLongClickArray[0] : mClickArray[0];
        } else if (x > 2 * limitX) {
            return isLong ? mLongClickArray[4] : mClickArray[4];
        } else if (y < limitY) {
            return isLong ? mLongClickArray[1] : mClickArray[1];
        } else if (y > 2 * limitY) {
            return isLong ? mLongClickArray[3] : mClickArray[3];
        } else if (!draweeView.retry()) {
            return isLong ? mLongClickArray[2] : mClickArray[2];
        }
        return 0;
    }

    private void doClickEvent(int value) {
        switch (value) {
            case EventUtils.EVENT_PREV_PAGE:
                prevPage();
                break;
            case EventUtils.EVENT_NEXT_PAGE:
                nextPage();
                break;
            case EventUtils.EVENT_SAVE_PICTURE:
                savePicture();
                break;
            case EventUtils.EVENT_LOAD_PREV:
                loadPrev();
                break;
            case EventUtils.EVENT_LOAD_NEXT:
                loadNext();
                break;
            case EventUtils.EVENT_EXIT_READER:
                exitReader();
                break;
            case EventUtils.EVENT_TO_FIRST:
                toFirst();
                break;
            case EventUtils.EVENT_TO_LAST:
                toLast();
                break;
            case EventUtils.EVENT_SWITCH_SCREEN:
                switchScreen();
                break;
            case EventUtils.EVENT_SWITCH_MODE:
                switchMode();
                break;
            case EventUtils.EVENT_SWITCH_CONTROL:
                switchControl();
                break;
        }
    }

    protected abstract void prevPage();

    protected abstract void nextPage();

    protected void savePicture() {
        String url = mReaderAdapter.getItem(mLayoutManager.findFirstVisibleItemPosition()).getUrl();
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

    protected void loadPrev() {
        mPresenter.loadPrev();
    }

    protected void loadNext() {
        mPresenter.loadNext();
    }

    protected void exitReader() {
        finish();
    }

    protected void toFirst() {
        mRecyclerView.scrollToPosition(0);
    }

    protected void toLast() {
        mRecyclerView.scrollToPosition(mReaderAdapter.getItemCount() - 1);
    }

    protected void switchScreen() {
        isPortrait = !isPortrait;
        int value = isPortrait ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        setRequestedOrientation(value);
    }

    protected void switchMode() {
        Intent intent = getIntent();
        boolean page = intent.getBooleanExtra(EXTRA_PAGE, true);
        if (page) {
            intent.setClass(this, StreamReaderActivity.class);
        } else {
            intent.setClass(this, PageReaderActivity.class);
        }
        intent.putExtra(EXTRA_PAGE, !page);
        finish();
        startActivity(intent);
    }

    protected void switchControl() {
        if (mProgressLayout.isShown()) {
            hideControl();
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

    private static final String EXTRA_ID = "a";
    private static final String EXTRA_CHAPTER = "b";
    private static final String EXTRA_PAGE = "c";

    public static Intent createIntent(Context context, long id, List<Chapter> list) {
        int mode = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        Intent intent = getIntent(context, mode);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_CHAPTER, list.toArray(new Chapter[list.size()]));
        intent.putExtra(EXTRA_PAGE, mode == PreferenceManager.READER_MODE_PAGE);
        return intent;
    }

    private static Intent getIntent(Context context, int mode) {
        if (mode == PreferenceManager.READER_MODE_PAGE) {
            return new Intent(context, PageReaderActivity.class);
        } else {
            return new Intent(context, StreamReaderActivity.class);
        }
    }

}
