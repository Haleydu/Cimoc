package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Selectable;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.ChapterListPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.adapter.ChapterListAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.SelectDialogFragment;
import com.hiroshi.cimoc.ui.view.ChapterListView;
import com.hiroshi.cimoc.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/11/14.
 */

public class ChapterListActivity extends BackActivity implements ChapterListView {

    @BindView(R.id.chapter_list_layout) View mLayoutView;
    @BindView(R.id.chapter_list_recycler_view) RecyclerView mRecyclerView;

    private List<Chapter> mDownloadList;
    private ChapterListPresenter mPresenter;
    private ChapterListAdapter mChapterAdapter;

    @Override
    protected void initPresenter() {
        mPresenter = new ChapterListPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        List<Chapter> list = getIntent().getParcelableArrayListExtra(EXTRA_CHAPTER);
        mChapterAdapter = new ChapterListAdapter(this, list);
    }

    @Override
    protected void initData() {
        mPresenter.loadDownload();
        mDownloadList = new ArrayList<>();
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
    }

    /*download(false, list);
        download(true, list);*/

    /**
     * download: load download -> select chapter -> check permission -> update index -> add task
     */

    private void download(boolean neutral, List<Selectable> list) {
        for (int i = 0; i != list.size(); ++i) {
            if ((neutral || list.get(i).isChecked()) && !list.get(i).isDisable()) {
                mDownloadList.add(mChapterAdapter.getItem(i));
            }
        }

        if (!mDownloadList.isEmpty()) {
            showProgressDialog();
            if (PermissionUtils.hasStoragePermission(this)) {
                mPresenter.updateIndex(mChapterAdapter.getDateSet());
            } else {
                onUpdateIndexFail();
            }
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
        Intent intent = DownloadService.createIntent(this, list);
        startService(intent);
        showSnackbar(R.string.detail_download_queue_success);
        hideProgressDialog();
    }

    @Override
    public void onTaskAddFail() {
        showSnackbar(R.string.detail_download_queue_fail);
        hideProgressDialog();
    }

    @Override
    public void onDownloadLoadSuccess(List<Task> list) {
        Set<String> set = new HashSet<>();
        for (Task task : list) {
            set.add(task.getPath());
        }
        for (Chapter chapter : mChapterAdapter.getDateSet()) {
            chapter.setDownload(set.contains(chapter.getPath()));
        }
        mChapterAdapter.notifyDataSetChanged();
        hideProgressDialog();
    }

    @Override
    public void onDownloadLoadFail() {
        showSnackbar(R.string.detail_download_load_fail);
        hideProgressBar();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_chapter_list;
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.chapter_list);
    }

    private static String EXTRA_CHAPTER = "a";

    public static Intent createIntent(Context context, ArrayList<Chapter> list) {
        Intent intent = new Intent(context, ChapterListActivity.class);
        intent.putExtra(EXTRA_CHAPTER, list);
        return intent;
    }

}
