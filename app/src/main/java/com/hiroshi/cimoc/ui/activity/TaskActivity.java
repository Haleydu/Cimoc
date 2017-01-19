package com.hiroshi.cimoc.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.TaskPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.service.DownloadService.DownloadServiceBinder;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.TaskAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.MultiDialogFragment;
import com.hiroshi.cimoc.ui.view.TaskView;
import com.hiroshi.cimoc.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public class TaskActivity extends CoordinatorActivity implements TaskView {

    private static final int DIALOG_REQUEST_DELETE = 0;

    private TaskAdapter mTaskAdapter;
    private TaskPresenter mPresenter;
    private ServiceConnection mConnection;
    private DownloadServiceBinder mBinder;
    private boolean mTaskOrder;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new TaskPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected BaseAdapter initAdapter() {
        mTaskAdapter = new TaskAdapter(this, new LinkedList<Task>());
        return mTaskAdapter;
    }

    @Override
    protected void initActionButton() {
        mActionButton.setImageResource(R.drawable.ic_launch_white_24dp);
        mActionButton.show();
    }

    @Override
    protected void initData() {
        long key = getIntent().getLongExtra(Extra.EXTRA_ID, -1);
        mTaskOrder = mPreference.getBoolean(PreferenceManager.PREF_DOWNLOAD_ORDER, false);
        mPresenter.load(key, mTaskOrder);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnection != null) {
            unbindService(mConnection);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        // Todo 从这里进去 如果改变了 last 会导致不一致 影响不大暂时不改
        Comic comic = mPresenter.getComic();
        Intent intent = DetailActivity.createIntent(this, comic.getId(), -1, null);
        startActivity(intent);
    }

    @Override
    public void onLastChange(String path) {
        mTaskAdapter.setLast(path);
    }

    @Override
    public void onItemClick(View view, final int position) {
        Task task = mTaskAdapter.getItem(position);
        switch (task.getState()) {
            case Task.STATE_FINISH:
                startReader(task.getPath());
                break;
            case Task.STATE_PAUSE:
            case Task.STATE_ERROR:
                task.setState(Task.STATE_WAIT);
                mTaskAdapter.notifyItemChanged(position);
                Intent taskIntent = DownloadService.createIntent(this, task);
                startService(taskIntent);
                break;
            case Task.STATE_WAIT:
                task.setState(Task.STATE_PAUSE);
                mTaskAdapter.notifyItemChanged(position);
                mBinder.getService().removeDownload(task.getId());
                break;
            case Task.STATE_DOING:
            case Task.STATE_PARSE:
                mBinder.getService().removeDownload(task.getId());
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.task_history:
                String path = mPresenter.getComic().getLast();
                if (path == null) {
                    path = mTaskAdapter.getItem(mTaskOrder ? 0 : mTaskAdapter.getDateSet().size() - 1).getPath();
                }
                startReader(path);
                break;
            case R.id.task_delete:
                int size = mTaskAdapter.getItemCount();
                String[] arr1 = new String[size];
                boolean[] arr2 = new boolean[size];
                for (int i = 0; i < size; ++i) {
                    arr1[i] = mTaskAdapter.getItem(i).getTitle();
                    arr2[i] = false;
                }
                MultiDialogFragment fragment = MultiDialogFragment.newInstance(R.string.task_delete, arr1, arr2, DIALOG_REQUEST_DELETE);
                fragment.show(getFragmentManager(), null);
                break;
            case R.id.task_sort:
                mTaskAdapter.reverse();
                mTaskOrder = !mTaskOrder;
                mPreference.putBoolean(PreferenceManager.PREF_DOWNLOAD_ORDER, mTaskOrder);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startReader(String path) {
        List<Chapter> list = new ArrayList<>();
        for (Task t : mTaskAdapter.getDateSet()) {
            if (t.getState() == Task.STATE_FINISH) {
                list.add(new Chapter(t.getTitle(), t.getPath(), t.getMax(), true, true));
            }
        }
        if (mTaskOrder) {
            Collections.reverse(list);
        }
        mTaskAdapter.setLast(path);
        long id = mPresenter.updateLast(path);
        int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        Intent readerIntent = ReaderActivity.createIntent(this, id, list, mode);
        startActivity(readerIntent);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_DELETE:
                boolean[] check = bundle.getBooleanArray(EXTRA_DIALOG_RESULT_VALUE);
                if (check != null) {
                    int size = mTaskAdapter.getItemCount();
                    List<Task> result = new ArrayList<>();
                    for (int i = 0; i < size; ++i) {
                        if (check[i]) {
                            result.add(mTaskAdapter.getItem(i));
                        }
                    }
                    if (!result.isEmpty()) {
                        showProgressDialog();
                        for (Task task : result) {
                            mBinder.getService().removeDownload(task.getId());
                        }
                        mPresenter.deleteTask(result, mTaskAdapter.getItemCount() == result.size());
                    }
                    break;
                }
                showSnackbar("未选择任务");
                break;
        }
    }

    @Override
    public void onTaskDeleteSuccess(List<Task> list) {
        hideProgressDialog();
        mTaskAdapter.removeAll(list);
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onTaskDeleteFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    public void onTaskLoadSuccess(final List<Task> list) {
        mTaskAdapter.setColorId(ThemeUtils.getResourceId(this, R.attr.colorAccent));
        mTaskAdapter.setLast(mPresenter.getComic().getLast());
        mTaskAdapter.addAll(list);
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = (DownloadServiceBinder) service;
                mBinder.getService().initTask(mTaskAdapter.getDateSet());
                hideProgressBar();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        bindService(new Intent(this, DownloadService.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onTaskLoadFail() {
        hideProgressBar();
        showSnackbar(R.string.task_load_task_fail);
    }

    @Override
    public void onTaskAdd(List<Task> list) {
        mTaskAdapter.addAll(0, list);
    }

    /**
     *  task state
     */

    @Override
    public void onTaskError(long id) {
        int position = mTaskAdapter.getPositionById(id);
        if (position != -1) {
            Task task = mTaskAdapter.getItem(position);
            if (task.getState() != Task.STATE_PAUSE) {
                task.setState(Task.STATE_ERROR);
                notifyItemChanged(position);
            }
        }
    }

    @Override
    public void onTaskPause(long id) {
        int position = mTaskAdapter.getPositionById(id);
        if (position != -1) {
            mTaskAdapter.getItem(position).setState(Task.STATE_PAUSE);
            notifyItemChanged(position);
        }
    }

    @Override
    public void onTaskParse(long id) {
        int position = mTaskAdapter.getPositionById(id);
        if (position != -1) {
            Task task = mTaskAdapter.getItem(position);
            if (task.getState() != Task.STATE_PAUSE) {
                task.setState(Task.STATE_PARSE);
                notifyItemChanged(position);
            }
        }
    }

    @Override
    public void onTaskProcess(long id, int progress, int max) {
        int position = mTaskAdapter.getPositionById(id);
        if (position != -1) {
            Task task = mTaskAdapter.getItem(position);
            task.setMax(max);
            task.setProgress(progress);
            if (task.getState() != Task.STATE_PAUSE) {
                int state = max == progress ? Task.STATE_FINISH : Task.STATE_DOING;
                task.setState(state);
            }
            notifyItemChanged(position);
        }
    }

    private void notifyItemChanged(int position) {
        if (!mRecyclerView.isComputingLayout()) {
            mTaskAdapter.notifyItemChanged(position);
        }
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.task_list);
    }

    public static Intent createIntent(Context context, Long id) {
        Intent intent = new Intent(context, TaskActivity.class);
        intent.putExtra(Extra.EXTRA_ID, id);
        return intent;
    }

}
