package com.hiroshi.cimoc.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.cache.common.SimpleCacheKey;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.fresco.ControllerBuilderFactory;
import com.hiroshi.cimoc.fresco.ImagePipelineFactoryBuilder;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.presenter.ReaderPresenter;
import com.hiroshi.cimoc.ui.adapter.ReaderAdapter;
import com.hiroshi.cimoc.ui.adapter.ReaderAdapter.OnLazyLoadListener;
import com.hiroshi.cimoc.ui.custom.PreCacheLayoutManager;
import com.hiroshi.cimoc.ui.custom.ReverseSeekBar;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnLongPressListener;
import com.hiroshi.cimoc.ui.custom.photo.PhotoDraweeViewController.OnSingleTapListener;
import com.hiroshi.cimoc.ui.view.ReaderView;
import com.hiroshi.cimoc.utils.StringUtils;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;
import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar.OnProgressChangeListener;

import java.io.IOException;
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
    @BindView(R.id.reader_seek_bar) ReverseSeekBar mSeekBar;
    @BindView(R.id.reader_mask) View mNightMask;
    @BindView(R.id.reader_recycler_view) RecyclerView mRecyclerView;

    protected PreCacheLayoutManager mLayoutManager;
    protected ReaderAdapter mReaderAdapter;
    protected ImagePipelineFactory mImagePipelineFactory;

    protected ReaderPresenter mPresenter;
    protected int progress = 1;
    protected int max = 1;

    private int source;

    @Override
    protected void initView() {
        if (CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_BRIGHT, false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_NIGHT, false)) {
            mNightMask.setVisibility(View.VISIBLE);
        }
        mSeekBar.setOnProgressChangeListener(this);
        List<ImageUrl> list = new LinkedList<>();
        list.add(new ImageUrl(0, null, true));
        mReaderAdapter = new ReaderAdapter(this, list);
        mReaderAdapter.setSingleTapListener(this);
        mReaderAdapter.setLazyLoadListener(this);
        if (CimocApplication.getPreferences().getBoolean(PreferenceManager.PREF_PICTURE, false)) {
            mReaderAdapter.setLongPressListener(this);
        }
        mImagePipelineFactory = ImagePipelineFactoryBuilder.build(this, source);
        mReaderAdapter.setControllerBuilder(ControllerBuilderFactory.get(this, mImagePipelineFactory));
        mLayoutManager = new PreCacheLayoutManager(this);
    }

    @Override
    protected void initData() {
        String last = getIntent().getStringExtra(EXTRA_LAST);
        int page = getIntent().getIntExtra(EXTRA_PAGE, -1);
        mPresenter.loadInit(last, page);
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
            mPresenter.setPage(progress);
        }
        unregisterReceiver(batteryReceiver);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @OnClick(R.id.reader_back_btn) void onBackClick() {
        onBackPressed();
    }

    @Override
    protected void initTheme() {}

    @Override
    protected void initToolbar() {}

    @Override
    protected void initPresenter() {
        source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        String cid = getIntent().getStringExtra(EXTRA_CID);
        String comic = getIntent().getStringExtra(EXTRA_COMIC);
        Parcelable[] array = getIntent().getParcelableArrayExtra(EXTRA_CHAPTER);
        Chapter[] chapter = new Chapter[array.length];
        for (int i = 0; i != array.length; ++i) {
            chapter[i] = (Chapter) array[i];
        }
        int position = getIntent().getIntExtra(EXTRA_POSITION, 0);
        mPresenter = new ReaderPresenter(source, cid, comic, chapter, position);
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
        }
    }

    protected void switchToolLayout() {
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

    protected void updateProgress() {
        mChapterPage.setText(StringUtils.getProgress(progress, max));
    }

    protected void showToast(int resId) {
        if (!isFinishing()) {
            Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onParseError() {
        showToast(R.string.common_parse_error);
    }

    @Override
    public void onNetworkError() {
        showToast(R.string.common_network_error);
    }

    @Override
    public void onNextLoadSuccess(List<ImageUrl> list) {
        mReaderAdapter.addAll(list);
        showToast(R.string.reader_load_success);
    }

    @Override
    public void onPrevLoadSuccess(List<ImageUrl> list) {
        mReaderAdapter.addAll(0, list);
        showToast(R.string.reader_load_success);
    }

    @Override
    public void onInitLoadSuccess(List<ImageUrl> list, int progress) {
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
        showToast(R.string.reader_picture_save_success);
    }

    @Override
    public void onPictureSaveFail() {
        showToast(R.string.reader_picture_save_fail);
    }

    @Override
    public void onPrevLoading() {
        showToast(R.string.reader_load_prev);
    }

    @Override
    public void onPrevLoadNone() {
        showToast(R.string.reader_prev_none);
    }

    @Override
    public void onNextLoading() {
        showToast(R.string.reader_load_next);
    }

    @Override
    public void onNextLoadNone() {
        showToast(R.string.reader_next_none);
    }

    protected void savePicture(int position) {
        String url = mReaderAdapter.getItem(position).getUrl();
        try {
            String suffix = StringUtils.getSplit(url, "\\.", -1);
            BinaryResource resource = mImagePipelineFactory.getMainFileCache().getResource(new SimpleCacheKey(url));
            if (resource != null) {
                mPresenter.savePicture(resource.openStream(), suffix);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showToast(R.string.reader_picture_save_fail);
        }
    }

    private static final String EXTRA_SOURCE = "a";
    private static final String EXTRA_CID = "b";
    private static final String EXTRA_LAST = "c";
    private static final String EXTRA_PAGE = "d";
    private static final String EXTRA_COMIC = "e";
    private static final String EXTRA_CHAPTER = "f";
    private static final String EXTRA_POSITION = "g";

    public static Intent createIntent(Context context, int source, String cid, String comic, List<Chapter> list, int position) {
        Intent intent = getIntent(context);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_CID, cid);
        intent.putExtra(EXTRA_COMIC, comic);
        intent.putExtra(EXTRA_CHAPTER, list.toArray(new Chapter[list.size()]));
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

    public static Intent createIntent(Context context, Comic comic, List<Chapter> list, int position) {
        Intent intent = getIntent(context);
        intent.putExtra(EXTRA_SOURCE, comic.getSource());
        intent.putExtra(EXTRA_CID, comic.getCid());
        intent.putExtra(EXTRA_LAST, comic.getLast());
        intent.putExtra(EXTRA_PAGE, comic.getPage());
        intent.putExtra(EXTRA_COMIC, comic.getTitle());
        intent.putExtra(EXTRA_CHAPTER, list.toArray(new Chapter[list.size()]));
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

    private static Intent getIntent(Context context) {
        int mode = CimocApplication.getPreferences().getInt(PreferenceManager.PREF_MODE, PreferenceManager.MODE_HORIZONTAL_PAGE);
        switch (mode) {
            default:
            case PreferenceManager.MODE_HORIZONTAL_PAGE:
                return new Intent(context, PageReaderActivity.class);
            case PreferenceManager.MODE_PORTRAIT_STREAM:
                return new Intent(context, StreamReaderActivity.class);
            case PreferenceManager.MODE_LANDSCAPE_STREAM:
                return new Intent(context, LandscapeStreamReaderActivity.class);
        }
    }

}
