package com.hiroshi.cimoc.ui.activity;

import android.app.Activity;
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
import com.hiroshi.cimoc.global.Extra;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.TaskPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.service.DownloadService.DownloadServiceBinder;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.TaskAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.ItemDialogFragment;
import com.hiroshi.cimoc.ui.view.TaskView;
import com.hiroshi.cimoc.utils.StringUtils;
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

    private static final int REQUEST_CODE_DELETE = 0;
    private static final int DIALOG_REQUEST_OPERATION = 1;

    private static final int OPERATION_READ = 0;
    private static final int OPERATION_DELETE = 1;

    private TaskAdapter mTaskAdapter;
    private TaskPresenter mPresenter;
    private ServiceConnection mConnection;
    private DownloadServiceBinder mBinder;
    private boolean mTaskOrder;

    private Task mSavedTask;

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
        mTaskOrder = mPreference.getBoolean(PreferenceManager.PREF_CHAPTER_ASCEND_MODE, false);
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
        Intent intent = DetailActivity.createIntent(this, mPresenter.getComic().getId(),
                mPresenter.getComic().getSource(), mPresenter.getComic().getCid());
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
                startReader(task.getPath(), false);
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
    public void onItemLongClick(View view, int position) {
        mSavedTask = mTaskAdapter.getItem(position);
        String[] item = { getString(R.string.task_read), getString(R.string.task_delete) };
        ItemDialogFragment fragment = ItemDialogFragment.newInstance(R.string.common_operation_select,
                item, DIALOG_REQUEST_OPERATION);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_OPERATION:
                int index = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                switch (index) {
                    case OPERATION_READ:
                        startReader(mSavedTask.getPath(), true);
                        break;
                    case OPERATION_DELETE:
                        showProgressDialog();
                        List<Chapter> list = new ArrayList<>(1);
                        list.add(new Chapter(mSavedTask.getTitle(), mSavedTask.getPath(), mSavedTask.getId()));
                        if (!mPresenter.getComic().getLocal()) {
                            mBinder.getService().removeDownload(mSavedTask.getId());
                        }
                        mPresenter.deleteTask(list, mTaskAdapter.getItemCount() == 1);
                        break;
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!mTaskAdapter.getDateSet().isEmpty()) {
            switch (item.getItemId()) {
                case R.id.task_history:
                    String path = mPresenter.getComic().getLast();
                    if (path == null) {
                        path = mTaskAdapter.getItem(mTaskOrder ?
                                0 : mTaskAdapter.getDateSet().size() - 1).getPath();
                    }
                    startReader(path, true);
                    break;
                case R.id.task_delete:
                    ArrayList<Chapter> list = new ArrayList<>(mTaskAdapter.getItemCount());
                    for (Task task : mTaskAdapter.getDateSet()) {
                        list.add(new Chapter(task.getTitle(), task.getPath(), task.getId()));
                    }
                    Intent intent = ChapterActivity.createIntent(this, list);
                    startActivityForResult(intent, REQUEST_CODE_DELETE);
                    break;
                case R.id.detail_search_title:
                    if (!StringUtils.isEmpty(mPresenter.getComic().getTitle())) {
                        intent = ResultActivity.createIntent(this, mPresenter.getComic().getTitle(),
                                null, ResultActivity.LAUNCH_MODE_SEARCH);
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.common_keyword_empty);
                    }
                    break;
                case R.id.detail_search_author:
                    if (!StringUtils.isEmpty(mPresenter.getComic().getAuthor())) {
                        intent = ResultActivity.createIntent(this, mPresenter.getComic().getAuthor(),
                                null, ResultActivity.LAUNCH_MODE_SEARCH);
                        startActivity(intent);
                    } else {
                        showSnackbar(R.string.common_keyword_empty);
                    }
                    break;
                case R.id.task_sort:
                    mTaskAdapter.reverse();
                    mTaskOrder = !mTaskOrder;
                    mPreference.putBoolean(PreferenceManager.PREF_CHAPTER_ASCEND_MODE, mTaskOrder);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void startReader(String path, boolean preview) {
        List<Chapter> list = new ArrayList<>();
        for (Task t : mTaskAdapter.getDateSet()) {
            if (preview && t.getProgress() > 0) {
                list.add(new Chapter(t.getTitle(), t.getPath(), t.getProgress(), true, true, t.getId()));
            } else if (t.getState() == Task.STATE_FINISH) {
                list.add(new Chapter(t.getTitle(), t.getPath(), t.getMax(), true, true, t.getId()));
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_DELETE:
                    List<Chapter> list = data.getParcelableArrayListExtra(Extra.EXTRA_CHAPTER);
                    if (!list.isEmpty()) {
                        showProgressDialog();
                        for (Chapter chapter : list) {
                            mBinder.getService().removeDownload(chapter.getTid());
                        }
                        mPresenter.deleteTask(list, mTaskAdapter.getItemCount() == list.size());
                    } else {
                        showSnackbar(R.string.task_empty);
                    }
                    break;
            }
        }
    }

    @Override
    public void onTaskDeleteSuccess(List<Long> list) {
        hideProgressDialog();
        mTaskAdapter.removeById(list);
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onTaskDeleteFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    public void onTaskLoadSuccess(final List<Task> list, boolean local) {
        mTaskAdapter.setColorId(ThemeUtils.getResourceId(this, R.attr.colorAccent));
        mTaskAdapter.setLast(mPresenter.getComic().getLast());
        mTaskAdapter.addAll(list);
        if (!local) {
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
        } else {
            hideProgressBar();
            mLayoutView.removeView(mActionButton);
        }
    }

    @Override
    public void onTaskLoadFail() {
        hideProgressBar();
        mLayoutView.removeView(mActionButton);
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
