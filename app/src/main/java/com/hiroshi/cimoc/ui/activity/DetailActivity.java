package com.hiroshi.cimoc.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.google.common.collect.Lists;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.ControllerBuilderSupplierFactory;
import com.hiroshi.cimoc.fresco.ImagePipelineFactoryBuilder;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.DetailAdapter;
import com.hiroshi.cimoc.ui.view.DetailView;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.OnClick;

import static com.hiroshi.cimoc.utils.interpretationUtils.isReverseOrder;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends CoordinatorActivity implements DetailView {

    public static final int REQUEST_CODE_DOWNLOAD = 0;

    private DetailAdapter mDetailAdapter;
    private DetailPresenter mPresenter;
    private ImagePipelineFactory mImagePipelineFactory;

    private boolean mAutoBackup;
    private int mBackupCount;

    public static Intent createIntent(Context context, Long id, int source, String cid) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(Extra.EXTRA_ID, id);
        intent.putExtra(Extra.EXTRA_SOURCE, source);
        intent.putExtra(Extra.EXTRA_CID, cid);
        return intent;
    }

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new DetailPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected BaseAdapter initAdapter() {
        mDetailAdapter = new DetailAdapter(this, new ArrayList<Chapter>());
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        return mDetailAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        return new GridLayoutManager(this, 3);
    }

    @Override
    protected void initData() {
        mAutoBackup = mPreference.getBoolean(PreferenceManager.PREF_BACKUP_SAVE_COMIC, true);
        mBackupCount = mPreference.getInt(PreferenceManager.PREF_BACKUP_SAVE_COMIC_COUNT, 0);
        long id = getIntent().getLongExtra(Extra.EXTRA_ID, -1);
        int source = getIntent().getIntExtra(Extra.EXTRA_SOURCE, -1);
        String cid = getIntent().getStringExtra(Extra.EXTRA_CID);
        mPresenter.load(id, source, cid);


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAutoBackup) {
            mPreference.putInt(PreferenceManager.PREF_BACKUP_SAVE_COMIC_COUNT, mBackupCount);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImagePipelineFactory != null) {
            mImagePipelineFactory.getImagePipeline().clearMemoryCaches();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
//                case R.id.detail_history:
//                    if (!mDetailAdapter.getDateSet().isEmpty()) {
//                        String path = mPresenter.getComic().getLast();
//                        if (path == null) {
//                            path = mDetailAdapter.getItem(mDetailAdapter.getDateSet().size() - 1).getPath();
//                        }
//                        startReader(path);
//                    }
//                    break;
                case R.id.detail_download:
                    if (!mDetailAdapter.getDateSet().isEmpty()) {
                        intent = ChapterActivity.createIntent(this, new ArrayList<>(mDetailAdapter.getDateSet()));
                        startActivityForResult(intent, REQUEST_CODE_DOWNLOAD);
                    }
                    break;
                case R.id.detail_tag:
                    if (mPresenter.getComic().getFavorite() != null) {
                        intent = TagEditorActivity.createIntent(this, mPresenter.getComic().getId());
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.detail_tag_favorite);
                    }
                    break;
                case R.id.detail_search_title:
                    if (!StringUtils.isEmpty(mPresenter.getComic().getTitle())) {
                        if(App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true)) {
                            Bundle bundle = new Bundle();
                            bundle.putString(FirebaseAnalytics.Param.CONTENT, mPresenter.getComic().getTitle());
                            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "byTitle");
                            bundle.putInt(FirebaseAnalytics.Param.SOURCE, mPresenter.getComic().getSource());
                            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SEARCH, bundle);
                        }
                        intent = ResultActivity.createIntent(this, mPresenter.getComic().getTitle(), null, ResultActivity.LAUNCH_MODE_SEARCH);
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.common_keyword_empty);
                    }
                    break;
                case R.id.detail_search_author:
                    if (!StringUtils.isEmpty(mPresenter.getComic().getAuthor())) {
                        intent = ResultActivity.createIntent(this, mPresenter.getComic().getAuthor(), null, ResultActivity.LAUNCH_MODE_SEARCH);
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.common_keyword_empty);
                    }
                    break;
                case R.id.detail_share_url:
                    String url = mPresenter.getComic().getUrl();
                    intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, url);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(Intent.createChooser(intent, url));

                    // firebase analytics
                    if(App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true)) {
                        Bundle bundle = new Bundle();
                        bundle.putString(FirebaseAnalytics.Param.CONTENT, url);
                        bundle.putInt(FirebaseAnalytics.Param.SOURCE, mPresenter.getComic().getSource());
                        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, bundle);
                    }
                    break;
                case R.id.detail_reverse_list:
                    mDetailAdapter.reverse();
                    break;
//                case R.id.detail_disqus:
//                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.home_page_cimqus_url) + "/cimoc/" + mPresenter.getComic().getTitle()));
//                    try {
//                        startActivity(intent);
//                    } catch (Exception e) {
//                        showSnackbar(R.string.about_resource_fail);
//                    }
//                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_DOWNLOAD:
                    showProgressDialog();
                    List<Chapter> list = data.getParcelableArrayListExtra(Extra.EXTRA_CHAPTER);
                    mPresenter.addTask(mDetailAdapter.getDateSet(), list);
                    break;
            }
        }
    }

    @OnClick(R.id.coordinator_action_button)
    void onActionButtonClick() {
        //todo: add comic to mangodb
        if (mPresenter.getComic().getFavorite() != null) {
            mPresenter.unfavoriteComic();
            increment();
            mActionButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            showSnackbar(R.string.detail_unfavorite);
        } else {
            mPresenter.favoriteComic();
            increment();
            mActionButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            showSnackbar(R.string.detail_favorite);
        }
    }

    @OnClick(R.id.coordinator_action_button2)
    void onActionButton2Click() {
        if (!mDetailAdapter.getDateSet().isEmpty()) {
            String path = mPresenter.getComic().getLast();
            if (path == null) {
                path = mDetailAdapter.getItem(mDetailAdapter.getDateSet().size() - 1).getPath();
            }
            startReader(path);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position != 0) {
            String path = mDetailAdapter.getItem(position - 1).getPath();
            startReader(path);
        }
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        if (position == 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(mDetailAdapter.title)
                    .setMessage(mDetailAdapter.intro)
                    .setPositiveButton(R.string.dialog_close, null)
                    .show();
        }
        return false;
    }


    private void startReader(String path) {
        long id = mPresenter.updateLast(path);
        mDetailAdapter.setLast(path);
        int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        Intent intent = ReaderActivity.createIntent(DetailActivity.this, id, mDetailAdapter.getDateSet(), mode);
        startActivity(intent);
    }

    @Override
    public void onLastChange(String last) {
        mDetailAdapter.setLast(last);
    }


    @Override
    public void onTaskAddSuccess(ArrayList<Task> list) {
        Intent intent = DownloadService.createIntent(this, list);
        startService(intent);
        updateChapterList(list);
        showSnackbar(R.string.detail_download_queue_success);
        hideProgressDialog();
    }

    private void updateChapterList(List<Task> list) {
        Set<String> set = new HashSet<>();
        for (Task task : list) {
            set.add(task.getPath());
        }
        for (Chapter chapter : mDetailAdapter.getDateSet()) {
            if (set.contains(chapter.getPath())) {
                chapter.setDownload(true);
            }
        }
    }

    @Override
    public void onTaskAddFail() {
        hideProgressDialog();
        showSnackbar(R.string.detail_download_queue_fail);
    }

    @Override
    public void onComicLoadSuccess(Comic comic) {
        mDetailAdapter.setInfo(comic.getCover(), comic.getTitle(), comic.getAuthor(),
                comic.getIntro(), comic.getFinish(), comic.getUpdate(), comic.getLast());

        if (comic.getTitle() != null && comic.getCover() != null) {
            mImagePipelineFactory = ImagePipelineFactoryBuilder.build(this, SourceManager.getInstance(this).getParser(comic.getSource()).getHeader(), false);
            mDetailAdapter.setControllerSupplier(ControllerBuilderSupplierFactory.get(this, mImagePipelineFactory));

            int resId = comic.getFavorite() != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
            mActionButton.setImageResource(resId);
            mActionButton.setVisibility(View.VISIBLE);
            mActionButton2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChapterLoadSuccess(List<Chapter> list) {
        hideProgressBar();
        if (mPresenter.getComic().getTitle() != null && mPresenter.getComic().getCover() != null) {
            mDetailAdapter.clear();
            mDetailAdapter.addAll(list);
            mDetailAdapter.notifyDataSetChanged();
        }
        if(App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true)) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT, mPresenter.getComic().getTitle());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Title");
            bundle.putInt(FirebaseAnalytics.Param.SOURCE, mPresenter.getComic().getSource());
            bundle.putBoolean(FirebaseAnalytics.Param.SUCCESS, true);
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
    }

    @Override
    public void onPreLoadSuccess(List<Chapter> list, Comic comic) {
        hideProgressBar();
        if (isReverseOrder(comic)){
            mDetailAdapter.addAll(Lists.reverse(list));
        }else {
            mDetailAdapter.addAll(list);
        }
        mDetailAdapter.setInfo(comic.getCover(), comic.getTitle(), comic.getAuthor(),
                comic.getIntro(), comic.getFinish(), comic.getUpdate(), comic.getLast());

        if (comic.getTitle() != null && comic.getCover() != null) {
            mImagePipelineFactory = ImagePipelineFactoryBuilder.build(this, SourceManager.getInstance(this).getParser(comic.getSource()).getHeader(), false);
            mDetailAdapter.setControllerSupplier(ControllerBuilderSupplierFactory.get(this, mImagePipelineFactory));

            int resId = comic.getFavorite() != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
            mActionButton.setImageResource(resId);
            mActionButton.setVisibility(View.VISIBLE);
            mActionButton2.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onParseError() {
        if(App.getPreferenceManager().getBoolean(PreferenceManager.PREF_OTHER_FIREBASE_EVENT, true)) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT, mPresenter.getComic().getTitle());
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Title");
            bundle.putInt(FirebaseAnalytics.Param.SOURCE, mPresenter.getComic().getSource());
            bundle.putBoolean(FirebaseAnalytics.Param.SUCCESS, false);
            FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, bundle);
        }
        hideProgressBar();
        showSnackbar(R.string.common_parse_error);


    }

    private void increment() {
        if (mAutoBackup && ++mBackupCount == 10) {
            mBackupCount = 0;
            mPreference.putInt(PreferenceManager.PREF_BACKUP_SAVE_COMIC_COUNT, 0);
            mPresenter.backup();
        }
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.detail);
    }

}
