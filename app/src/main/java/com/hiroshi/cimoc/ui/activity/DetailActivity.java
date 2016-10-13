package com.hiroshi.cimoc.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import com.hiroshi.cimoc.ui.fragment.dialog.SelectDialogFragment;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends BackActivity implements DetailView, DetailAdapter.OnTitleClickListener,
        SelectDialogFragment.SelectDialogListener {

    @BindView(R.id.detail_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.detail_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.detail_action_button) FloatingActionButton mStarButton;

    private DetailAdapter mDetailAdapter;
    private DetailPresenter mPresenter;
    private ArrayList<Selectable> mSelectList;
    private List<Chapter> mTempList;

    @Override
    protected void initPresenter() {
        mPresenter = new DetailPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mTempList = new LinkedList<>();
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
        mPresenter.updateLast(last);
        int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        Intent intent = ReaderActivity.createIntent(DetailActivity.this, mPresenter.getComic().getId(), mode, list);
        startActivity(intent);
    }

    @Override
    public void onChapterChange(String last) {
        mDetailAdapter.setLast(last);
    }

    /**
     * download: select chapter -> check permission -> update index -> add task
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail_download:
                if (mSelectList != null) {
                    SelectDialogFragment fragment =
                            SelectDialogFragment.newInstance(mSelectList, R.string.detail_select_chapter, R.string.detail_download_all);
                    fragment.show(getFragmentManager(), null);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectPositiveClick(int type, List<Selectable> list) {
        boolean isEmpty = true;
        for (int i = 0; i != mSelectList.size(); ++i) {
            if (mSelectList.get(i).isChecked() && !mSelectList.get(i).isDisable()) {
                mTempList.add(mDetailAdapter.getItem(i));
                isEmpty = false;
            }
        }
        download(isEmpty);
    }

    @Override
    public void onSelectNeutralClick(int type, List<Selectable> list) {
        boolean isEmpty = true;
        for (int i = 0; i != mSelectList.size(); ++i) {
            if (!mSelectList.get(i).isChecked()) {
                mTempList.add(mDetailAdapter.getItem(i));
                isEmpty = false;
            }
        }
        download(isEmpty);
    }

    private void download(boolean isEmpty) {
        if (!isEmpty) {
            mProgressDialog.show();
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
        mPresenter.addTask(mTempList);
    }

    @Override
    public void onUpdateIndexFail() {
        mProgressDialog.hide();
        mTempList.clear();
        showSnackbar(R.string.detail_download_queue_fail);
    }

    @Override
    public void onTaskAddSuccess(ArrayList<Task> list) {
        Intent intent = DownloadService.createIntent(DetailActivity.this, list);
        startService(intent);
        for (Selectable selectable : mSelectList) {
            if (selectable.isChecked()) {
                selectable.setDisable(true);
            }
        }
        mTempList.clear();
        mProgressDialog.hide();
        showSnackbar(R.string.detail_download_queue_success);
    }

    @Override
    public void onTaskAddFail() {
        mProgressDialog.hide();
        showSnackbar(R.string.detail_download_queue_fail);
    }

    /**
     *  init: load comic -> load chapter -> load download
     *  if load download fail, we show the layout without accent chapter button
     */

    @Override
    public void onComicLoad(Comic comic) {
        mDetailAdapter = new DetailAdapter(this, new LinkedList<Chapter>());
        mDetailAdapter.setInfo(comic.getSource(), comic.getCover(), comic.getTitle(), comic.getAuthor(),
                comic.getIntro(), comic.getFinish(), comic.getUpdate(), comic.getLast());
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        mRecyclerView.setAdapter(mDetailAdapter);
        mRecyclerView.addItemDecoration(mDetailAdapter.getItemDecoration());

        if (comic.getTitle() != null && comic.getCover() != null) {
            int resId = comic.getFavorite() != null ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
            mStarButton.setImageResource(resId);
            mStarButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChapterLoad(final List<Chapter> list) {
        mDetailAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position != 0) {
                    String last = mDetailAdapter.getItem(position - 1).getPath();
                    mDetailAdapter.setLast(last);
                    long id = mPresenter.updateLast(last);
                    int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
                    Intent intent = ReaderActivity.createIntent(DetailActivity.this, id, mode, mDetailAdapter.getDateSet());
                    startActivity(intent);
                }
            }
        });
        mDetailAdapter.setOnTitleClickListener(this);
        mDetailAdapter.setData(list);
    }

    @Override
    public void onDetailLoadSuccess() {
        // loadDownload will set the 'download' field of chapter
        mPresenter.loadDownload(mDetailAdapter.getDateSet());
    }

    @Override
    public void onDownloadLoadSuccess(ArrayList<Selectable> select) {
        hideProgressBar();
        mSelectList = select;
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

    public static Intent createIntent(Context context, Long id, int source, String cid) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_CID, cid);
        return intent;
    }

}
