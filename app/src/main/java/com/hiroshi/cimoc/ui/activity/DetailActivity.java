package com.hiroshi.cimoc.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.DetailPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.DetailAdapter;
import com.hiroshi.cimoc.ui.adapter.SelectAdapter;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class DetailActivity extends BackActivity implements DetailView {

    @BindView(R.id.detail_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.detail_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.detail_star_btn) FloatingActionButton mStarButton;

    private DetailAdapter mDetailAdapter;
    private SelectAdapter mSelectAdapter;
    private DetailPresenter mPresenter;

    @Override
    protected void initPresenter() {
        int source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        mPresenter = new DetailPresenter(source);
        mPresenter.attachView(this);
    }

    @Override
    protected void initData() {
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        String cid = getIntent().getStringExtra(EXTRA_CID);
        mPresenter.loadDetail(id, cid);
    }

    @Override
    protected void onDestroy() {
        mPresenter.updateComic();
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail_download:
                if (mSelectAdapter != null && mSelectAdapter.getItemCount() != 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    View view = getLayoutInflater().inflate(R.layout.dialog_select_chapter, null);
                    RecyclerView recyclerView = ButterKnife.findById(view, R.id.chapter_recycler_view);
                    recyclerView.setLayoutManager(new LinearLayoutManager(this));
                    recyclerView.setHasFixedSize(true);
                    recyclerView.setAdapter(mSelectAdapter);
                    builder.setTitle(R.string.detail_select_chapter);
                    builder.setView(view);
                    builder.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            download();
                        }
                    });
                    builder.setNeutralButton(R.string.detail_download_all, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSelectAdapter.checkAll();
                            download();
                        }
                    });
                    builder.show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
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

    private void download() {
        List<Integer> list = mSelectAdapter.getCheckedList();
        if (!list.isEmpty()) {
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

    @OnClick(R.id.detail_star_btn) void onClick() {
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
    public void onChapterChange(String last) {
        mDetailAdapter.setLast(last);
    }

    @Override
    public void onDownloadLoadSuccess(boolean[] download, boolean[] complete) {
        mSelectAdapter = new SelectAdapter(this, mDetailAdapter.getTitles());
        mSelectAdapter.initState(download);
        mSelectAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                CheckBox box = ButterKnife.findById(view, R.id.download_chapter_checkbox);
                if (box.isEnabled()) {
                    boolean checked = !box.isChecked();
                    box.setChecked(checked);
                    mSelectAdapter.onClick(position, checked);
                }
            }
        });
        mDetailAdapter.setDownload(complete);
        hideProgressBar();
    }

    @Override
    public void onDownloadLoadFail() {
        hideProgressBar();
        showSnackbar(R.string.detail_download_load_fail);
    }

    @Override
    public void onTaskAddSuccess(ArrayList<Task> list) {
        Intent intent = DownloadService.createIntent(DetailActivity.this, list);
        startService(intent);
        mSelectAdapter.refreshChecked();
        mProgressDialog.hide();
        showSnackbar(R.string.detail_download_queue_success);
    }

    @Override
    public void onTaskAddFail() {
        mProgressDialog.hide();
        showSnackbar(R.string.detail_download_queue_fail);
    }

    @Override
    public void onUpdateIndexSuccess() {
        List<Integer> checked = mSelectAdapter.getCheckedList();
        List<Chapter> list = new ArrayList<>(checked.size());
        for (int position : checked) {
            list.add(mDetailAdapter.getItem(position));
        }
        mPresenter.addTask(list);
    }

    @Override
    public void onUpdateIndexFail() {
        mProgressDialog.hide();
        showSnackbar(R.string.detail_download_queue_fail);
    }

    @Override
    public void onDetailLoadSuccess() {
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        mPresenter.loadDownload(id, mDetailAdapter.getPaths());
    }

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
            if (comic.getFavorite() != null) {
                mStarButton.setImageResource(R.drawable.ic_favorite_white_24dp);
            } else {
                mStarButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
            }
            mStarButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onChapterLoad(List<Chapter> list) {
        mDetailAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (position != 0) {
                    Intent intent = ReaderActivity.createIntent(DetailActivity.this, mPresenter.getComic(),
                            mDetailAdapter.getDateSet(), position - 1);
                    startActivity(intent);
                }
            }
        });
        mDetailAdapter.setData(list);
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
