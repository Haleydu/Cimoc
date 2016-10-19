package com.hiroshi.cimoc.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Selectable;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.DetailAdapter;
import com.hiroshi.cimoc.ui.fragment.ComicFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.SelectDialogFragment;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends BackActivity implements DetailView, DetailAdapter.OnTitleClickListener,
        SelectDialogFragment.SelectDialogListener {

    private static final int TYPE_SELECT_DOWNLOAD = 0;
    private static final int TYPE_SELECT_TAG = 1;

    @BindView(R.id.detail_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.detail_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.detail_action_button) FloatingActionButton mStarButton;

    private DetailAdapter mDetailAdapter;
    private DetailPresenter mPresenter;
    private List<Chapter> mDownloadList;

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
    }

    @Override
    protected void initData() {
        mDownloadList = new ArrayList<>();
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        String cid = getIntent().getStringExtra(EXTRA_CID);
        mPresenter.loadDetail(id, source, cid);
    }

    @Override
    protected void onDestroy() {
        mPresenter.updateComic();
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
                    showProgressDialog();
                    mPresenter.loadDownload();
                    break;
                case R.id.detail_tag:
                    if (mPresenter.getComic().getFavorite() != null) {
                        showProgressDialog();
                        mPresenter.loadTag();
                    } else {
                        showSnackbar(R.string.detail_tag_favorite);
                    }
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.detail_action_button) void onClick() {
        if (mPresenter.getComic().getFavorite() != null) {
            mPresenter.unfavoriteComic();
            mStarButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            showSnackbar(R.string.detail_unfavorite);
        } else {
            mPresenter.favoriteComic();
            mStarButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            showSnackbar(R.string.detail_favorite);
        }
    }

    @Override
    public void onTitleClick() {
        String last = mPresenter.getComic().getLast();
        List<Chapter> list = mDetailAdapter.getDateSet();
        if (last == null) {
            last = mDetailAdapter.getItem(list.size() - 1).getPath();
            mDetailAdapter.setLast(last);
        }
        int type = getIntent().getIntExtra(EXTRA_TYPE, ComicFragment.TYPE_HISTORY);
        mPresenter.updateLast(last, type);
        int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        Intent intent = ReaderActivity.createIntent(DetailActivity.this, mPresenter.getComic().getId(), mode, list);
        startActivity(intent);
    }

    @Override
    public void onChapterChange(String last) {
        mDetailAdapter.setLast(last);
    }

    @Override
    public void onTagLoadSuccess(List<Selectable> list) {
        SelectDialogFragment fragment =
                SelectDialogFragment.newInstance(new ArrayList<>(list), R.string.detail_select_tag, -1, TYPE_SELECT_TAG);
        fragment.show(getFragmentManager(), null);
        hideProgressDialog();
    }

    @Override
    public void onTagLoadFail() {
        showSnackbar(R.string.detail_tag_load_fail);
        hideProgressDialog();
    }

    @Override
    public void onTagUpdateSuccess() {
        showSnackbar(R.string.detail_tag_update_success);
        hideProgressDialog();
    }

    @Override
    public void onTagUpdateFail() {
        showSnackbar(R.string.detail_tag_update_fail);
        hideProgressDialog();
    }

    @Override
    public void onSelectPositiveClick(int type, List<Selectable> list) {
        if (type == TYPE_SELECT_TAG) {
            List<Long> newTagList = new LinkedList<>();
            for (Selectable selectable : list) {
                if (selectable.isChecked()) {
                    newTagList.add(selectable.getId());
                }
            }
            showProgressDialog();
            mPresenter.updateRef(newTagList);
        } else {
            for (int i = 0; i != list.size(); ++i) {
                if (list.get(i).isChecked() && !list.get(i).isDisable()) {
                    mDownloadList.add(mDetailAdapter.getItem(i));
                }
            }
            download();
        }
    }

    @Override
    public void onSelectNeutralClick(int type, List<Selectable> list) {
        for (int i = 0; i != list.size(); ++i) {
            if (!list.get(i).isDisable()) {
                mDownloadList.add(mDetailAdapter.getItem(i));
            }
        }
        download();
    }

    /**
     * download: load download -> select chapter -> check permission -> update index -> add task
     */

    private void download() {
        if (!mDownloadList.isEmpty()) {
            showProgressDialog();
            if (ContextCompat.checkSelfPermission(DetailActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DetailActivity.this, new String[]
                        {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            } else {
                mPresenter.updateIndex(mDetailAdapter.getDateSet());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPresenter.updateIndex(mDetailAdapter.getDateSet());
                } else {
                    onUpdateIndexFail();
                }
                break;
        }
    }

    @Override
    public void onUpdateIndexSuccess() {
        mPresenter.addTask(mDownloadList);
    }

    @Override
    public void onUpdateIndexFail() {
        showSnackbar(R.string.detail_download_queue_fail);
        hideProgressDialog();
    }

    @Override
    public void onTaskAddSuccess(ArrayList<Task> list) {
        Intent intent = DownloadService.createIntent(DetailActivity.this, list);
        startService(intent);
        showSnackbar(R.string.detail_download_queue_success);
        hideProgressDialog();
    }

    @Override
    public void onTaskAddFail() {
        showSnackbar(R.string.detail_download_queue_fail);
        hideProgressDialog();
    }

    /**
     *  init: load comic -> load chapter -> load download
     *  if load download fail, we still show the layout
     */

    @Override
    public void onComicLoadSuccess(Comic comic) {
        mDetailAdapter.setInfo(comic.getSource(), comic.getCover(), comic.getTitle(), comic.getAuthor(),
                comic.getIntro(), comic.getFinish(), comic.getUpdate(), comic.getLast());

        if (comic.getTitle() != null && comic.getCover() != null) {
            int resId = comic.getFavorite() != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
            mStarButton.setImageResource(resId);
            mStarButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChapterLoadSuccess(List<Chapter> list) {
        mDetailAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position != 0) {
                    String last = mDetailAdapter.getItem(position - 1).getPath();
                    mDetailAdapter.setLast(last);
                    int type = getIntent().getIntExtra(EXTRA_TYPE, ComicFragment.TYPE_HISTORY);
                    long id = mPresenter.updateLast(last, type);
                    int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
                    Intent intent = ReaderActivity.createIntent(DetailActivity.this, id, mode, mDetailAdapter.getDateSet());
                    startActivity(intent);
                }
            }
        });
        mDetailAdapter.setOnTitleClickListener(this);
        mDetailAdapter.addAll(list);
    }

    @Override
    public void onDetailLoadSuccess() {
        mPresenter.loadDownload();
    }

    @Override
    public void onDownloadLoadSuccess(Set<String> set) {
        if (isProgressBarShown()) {
            for (Chapter chapter : mDetailAdapter.getDateSet()) {
                chapter.setDownload(set.contains(chapter.getPath()));
            }
            hideProgressBar();
        } else {
            mDownloadList.clear();
            ArrayList<Selectable> list = new ArrayList<>();
            for (Chapter chapter : mDetailAdapter.getDateSet()) {
                boolean download = set.contains(chapter.getPath());
                list.add(new Selectable(download, download, chapter.getTitle()));
            }
            hideProgressDialog();
            SelectDialogFragment fragment = SelectDialogFragment.newInstance(list, R.string.detail_select_chapter,
                            R.string.detail_download_all, TYPE_SELECT_DOWNLOAD);
            fragment.show(getFragmentManager(), null);
        }
    }

    @Override
    public void onDownloadLoadFail() {
        showSnackbar(R.string.detail_download_load_fail);
        hideProgressBar();
    }

    @Override
    public void onNetworkError() {
        showSnackbar(R.string.common_network_error);
        hideProgressBar();
    }

    @Override
    public void onParseError() {
        showSnackbar(R.string.common_parse_error);
        hideProgressBar();
    }

    /**
     *  method we don't care
     */

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_detail;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.detail);
    }

    @Override
    protected View getLayoutView() {
        return mCoordinatorLayout;
    }

    public static final String EXTRA_ID = "a";
    public static final String EXTRA_SOURCE = "b";
    public static final String EXTRA_CID = "c";
    public static final String EXTRA_TYPE = "d";

    public static Intent createIntent(Context context, Long id, int source, String cid) {
        return createIntent(context, id, source, cid, ComicFragment.TYPE_HISTORY);
    }

    public static Intent createIntent(Context context, Long id, int source, String cid, int type) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_CID, cid);
        intent.putExtra(EXTRA_TYPE, type);
        return intent;
    }

}
