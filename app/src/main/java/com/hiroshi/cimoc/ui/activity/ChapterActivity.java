package com.hiroshi.cimoc.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.ChapterPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.adapter.ChapterAdapter;
import com.hiroshi.cimoc.ui.view.ChapterView;
import com.hiroshi.cimoc.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/11/14.
 */

public class ChapterActivity extends CoordinatorActivity implements ChapterView {

    private ChapterPresenter mPresenter;
    private ChapterAdapter mChapterAdapter;

    @Override
    protected void initPresenter() {
        mPresenter = new ChapterPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mChapterAdapter = new ChapterAdapter(this, getAdapterList());
        mChapterAdapter.setOnItemClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mChapterAdapter);
        mActionButton.setImageResource(R.drawable.ic_done_white_24dp);
        mActionButton.show();
        hideProgressBar();
    }

    @Override
    protected void initData() {
        long id = getIntent().getLongExtra(EXTRA_ID, -1);
        mPresenter.load(id);
    }

    private List<Pair<Chapter, Boolean>> getAdapterList() {
        List<Chapter> list = getIntent().getParcelableArrayListExtra(EXTRA_CHAPTER);
        List<Pair<Chapter, Boolean>> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); ++i) {
            result.add(Pair.create(list.get(i), list.get(i).isDownload()));
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chapter_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isProgressBarShown()) {
            switch (item.getItemId()) {
                case R.id.chapter_all:
                    for (Pair<Chapter, Boolean> pair : mChapterAdapter.getDateSet()) {
                        pair.second = true;
                    }
                    mChapterAdapter.notifyDataSetChanged();
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, int position) {
        Pair<Chapter, Boolean> pair = mChapterAdapter.getItem(position);
        if (!pair.first.isDownload()) {
            pair.second = !pair.second;
            mChapterAdapter.notifyItemChanged(position);
        }
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        List<Chapter> cList = new ArrayList<>();
        List<Chapter> dList = new ArrayList<>();
        for (Pair<Chapter, Boolean> pair : mChapterAdapter.getDateSet()) {
            cList.add(pair.first);
            if (!pair.first.isDownload() && pair.second) {
                dList.add(pair.first);
            }
        }

        if (dList.isEmpty()) {
            showSnackbar(R.string.chapter_download_empty);
        } else if (PermissionUtils.hasStoragePermission(this)) {
            showProgressDialog();
            mPresenter.addTask(cList, dList);
        } else {
            onTaskAddFail();
        }
    }

    @Override
    public void onTaskAddSuccess(ArrayList<Task> list) {
        Intent intent = DownloadService.createIntent(this, list);
        startService(intent);
        for (Pair<Chapter, Boolean> pair : mChapterAdapter.getDateSet()) {
            if (pair.second && !pair.first.isDownload()) {
                pair.first.setDownload(true);
            }
        }
        mChapterAdapter.notifyDataSetChanged();
        showSnackbar(R.string.chapter_download_queue_success);
        hideProgressDialog();
    }

    @Override
    public void onTaskAddFail() {
        hideProgressDialog();
        showSnackbar(R.string.chapter_download_queue_fail);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.chapter);
    }

    private static String EXTRA_ID = "a";
    private static String EXTRA_CHAPTER = "b";

    public static Intent createIntent(Context context, long id, ArrayList<Chapter> list) {
        Intent intent = new Intent(context, ChapterActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_CHAPTER, list);
        return intent;
    }

}
