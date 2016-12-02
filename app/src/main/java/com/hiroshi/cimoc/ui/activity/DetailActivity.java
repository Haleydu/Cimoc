package com.hiroshi.cimoc.ui.activity;

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
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.ui.adapter.DetailAdapter;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends CoordinatorActivity implements DetailView, DetailAdapter.OnTitleClickListener {

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
        mDetailAdapter = new DetailAdapter(this, new LinkedList<Chapter>());
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mRecyclerView.setAdapter(mDetailAdapter);
        mRecyclerView.addItemDecoration(mDetailAdapter.getItemDecoration());
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    @Override
    protected void initData() {
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        if (id == -1) {
            int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
            String cid = getIntent().getStringExtra(EXTRA_CID);
            mPresenter.loadDetail(source, cid);
        } else {
            mPresenter.loadDetail(id);
        }
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
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
                case R.id.detail_download:
                    Intent intent = ChapterActivity.createIntent(this, mPresenter.getId(), new ArrayList<>(mDetailAdapter.getDateSet()));
                    startActivity(intent);
                    break;
                case R.id.detail_tag:
                    mPresenter.onTagClick();
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        mPresenter.onFavoriteClick();
    }

    @Override
    public void onTagOpenFail() {
        showSnackbar(R.string.detail_tag_favorite);
    }

    @Override
    public void onTagOpenSuccess() {
        Intent intent = TagEditorActivity.createIntent(this, mPresenter.getId());
        startActivity(intent);
    }

    @Override
    public void onFavoriteSuccess() {
        mActionButton.setImageResource(R.drawable.ic_favorite_white_24dp);
        showSnackbar(R.string.detail_favorite);
    }

    @Override
    public void onUnfavoriteSuccess() {
        mActionButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
        showSnackbar(R.string.detail_unfavorite);
    }

    @Override
    public void onItemClick(View view, int position) {
        if (position != 0) {
            String last = mDetailAdapter.getItem(position - 1).getPath();
            mPresenter.updateLast(last, getIntent().getBooleanExtra(EXTRA_FAVORITE, false));
            mDetailAdapter.setLast(last);
            int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
            Intent intent = ReaderActivity.createIntent(this, mPresenter.getId(), mDetailAdapter.getDateSet(), mode);
            startActivity(intent);
        }
    }

    @Override
    public void onTitleClick() {
        String path = mDetailAdapter.getItem(mDetailAdapter.getDateSet().size() - 1).getPath();
        boolean favorite = getIntent().getBooleanExtra(EXTRA_FAVORITE, false);
        mPresenter.onTitleClick(path, favorite);
    }

    @Override
    public void onLastOpenSuccess(String path) {
        mDetailAdapter.setLast(path);
        List<Chapter> list = mDetailAdapter.getDateSet();
        int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        Intent intent = ReaderActivity.createIntent(DetailActivity.this, mPresenter.getId(), list, mode);
        startActivity(intent);
    }

    @Override
    public void onChapterChange(String last) {
        mDetailAdapter.setLast(last);
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
    public void onDownloadLoadFail() {
        hideProgressBar();
        showSnackbar(R.string.detail_download_load_fail);
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
