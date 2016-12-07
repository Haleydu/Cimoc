package com.hiroshi.cimoc.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.adapter.DetailAdapter;
import com.hiroshi.cimoc.ui.view.DetailView;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends CoordinatorActivity implements DetailView, DetailAdapter.OnTitleClickListener {

    public static final int REQUEST_CODE_DOWNLOAD = 0;

    private DetailAdapter mDetailAdapter;
    private DetailPresenter mPresenter;

    @Override
    protected void initPresenter() {
        mPresenter = new DetailPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mDetailAdapter = new DetailAdapter(this, new ArrayList<Chapter>());
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mRecyclerView.setAdapter(mDetailAdapter);
        mRecyclerView.addItemDecoration(mDetailAdapter.getItemDecoration());
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    @Override
    protected void initData() {
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        String cid = getIntent().getStringExtra(EXTRA_CID);
        mPresenter.load(id, source, cid);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
                case R.id.detail_download:
                    intent = ChapterActivity.createIntent(this, new ArrayList<>(mDetailAdapter.getDateSet()));
                    startActivityForResult(intent, REQUEST_CODE_DOWNLOAD);
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
                        intent = ResultActivity.createIntent(this, mPresenter.getComic().getTitle(), null);
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.detail_search_empty);
                    }
                    break;
                case R.id.detail_search_author:
                    if (!StringUtils.isEmpty(mPresenter.getComic().getTitle())) {
                        intent = ResultActivity.createIntent(this, mPresenter.getComic().getAuthor(), null);
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.detail_search_empty);
                    }
                    break;
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
                    List<Chapter> list = data.getParcelableArrayListExtra(ChapterActivity.EXTRA_CHAPTER);
                    mPresenter.addTask(mDetailAdapter.getDateSet(), list);
                    break;
            }
        }
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        if (mPresenter.getComic().getFavorite() != null) {
            mPresenter.unfavoriteComic();
            mActionButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            showSnackbar(R.string.detail_unfavorite);
        } else {
            mPresenter.favoriteComic();
            mActionButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            showSnackbar(R.string.detail_favorite);
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
    public void onTitleClick() {
        String path = mPresenter.getComic().getLast();
        if (path != null) {
            path = mDetailAdapter.getItem(mDetailAdapter.getDateSet().size() - 1).getPath();
        }
        startReader(path);
    }

    private void startReader(String path) {
        boolean favorite = getIntent().getBooleanExtra(EXTRA_FAVORITE, false);
        long id = mPresenter.updateLast(path, favorite);
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
        mDetailAdapter.setInfo(comic.getSource(), comic.getCover(), comic.getTitle(), comic.getAuthor(),
                comic.getIntro(), comic.getFinish(), comic.getUpdate(), comic.getLast());

        if (comic.getTitle() != null && comic.getCover() != null) {
            int resId = comic.getFavorite() != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
            mActionButton.setImageResource(resId);
            mActionButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChapterLoadSuccess(List<Chapter> list) {
        hideProgressBar();
        mDetailAdapter.setOnItemClickListener(this);
        mDetailAdapter.setOnTitleClickListener(this);
        mDetailAdapter.addAll(list);
    }

    @Override
    public void onNetworkError() {
        hideProgressBar();
        showSnackbar(R.string.common_network_error);
    }

    @Override
    public void onParseError() {
        hideProgressBar();
        showSnackbar(R.string.common_parse_error);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.detail);
    }

    public static final String EXTRA_ID = "a";
    public static final String EXTRA_SOURCE = "b";
    public static final String EXTRA_CID = "c";
    public static final String EXTRA_FAVORITE = "d";

    public static Intent createIntent(Context context, Long id, int source, String cid, boolean favorite) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_CID, cid);
        intent.putExtra(EXTRA_FAVORITE, favorite);
        return intent;
    }

}
