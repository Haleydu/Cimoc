package com.hiroshi.cimoc.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.TaskPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.service.DownloadService.DownloadServiceBinder;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.TaskAdapter;
import com.hiroshi.cimoc.ui.view.TaskView;
import com.hiroshi.cimoc.utils.CollectionUtils;
import com.hiroshi.cimoc.utils.DialogUtils;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public class TaskActivity extends BackActivity implements TaskView, BaseAdapter.OnItemClickListener, BaseAdapter.OnItemLongClickListener {

    @BindView(R.id.task_layout) View mTaskLayout;
    @BindView(R.id.task_recycler_view) RecyclerView mRecyclerView;

    private TaskAdapter mTaskAdapter;
    private TaskPresenter mPresenter;
    private ServiceConnection mConnection;
    private DownloadServiceBinder mBinder;

    @Override
    protected void initPresenter() {
        mPresenter = new TaskPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mTaskAdapter = new TaskAdapter(this, new LinkedList<Task>());
        mTaskAdapter.setOnItemClickListener(this);
        mTaskAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(mTaskAdapter.getItemDecoration());
    }

    @Override
    protected void initData() {
        long key = getIntent().getLongExtra(EXTRA_KEY, -1);
        mPresenter.load(key);
    }

    @Override
    protected void onDestroy() {
        if (mConnection != null) {
            unbindService(mConnection);
        }
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.task_delete_multi:
                String[] chapter = mTaskAdapter.getTaskTitle();
                final boolean[] checked = new boolean[chapter.length];
                DialogUtils.buildMultiChoiceDialog(this, R.string.task_delete_multi, chapter, checked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                checked[which] = isChecked;
                            }
                        }, R.string.task_delete_all, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mProgressDialog.show();
                                deleteTask(new LinkedList<>(mTaskAdapter.getDateSet()));
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mProgressDialog.show();
                                List<Task> list = CollectionUtils.findAllToList(mTaskAdapter.getDateSet(), new CollectionUtils.Condition<Task>() {
                                    @Override
                                    public boolean call(int position, Task element) {
                                        return checked[position];
                                    }
                                });
                                deleteTask(list);
                            }
                        }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteTask(List<Task> list) {
        if (list.isEmpty()) {
            mProgressDialog.hide();
        } else {
            for (Task task : list) {
                mBinder.getService().removeDownload(task.getId());
            }
            mPresenter.deleteTask(list, mTaskAdapter.getItemCount() == list.size());
            mTaskAdapter.removeAll(list);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        Task task = mTaskAdapter.getItem(position);
        switch (task.getState()) {
            case Task.STATE_FINISH:
                final String path = mTaskAdapter.getItem(position).getPath();
                List<Chapter> list = CollectionUtils.findAllToList(mTaskAdapter.getDateSet(), new CollectionUtils.Condition<Task>() {
                    @Override
                    public boolean call(int position, Task element) {
                        return element.getState() == Task.STATE_FINISH;
                    }
                }, new CollectionUtils.Construct<Task, Chapter>() {
                    @Override
                    public Chapter call(Task element) {
                        return new Chapter(element.getTitle(), element.getPath(), element.getMax(), true);
                    }
                });
                int pos = CollectionUtils.findFirstFromList(list, new CollectionUtils.Condition<Chapter>() {
                    @Override
                    public boolean call(int position, Chapter element) {
                        return element.getPath().equals(path);
                    }
                });
                Intent readerIntent = ReaderActivity.createIntent(this, mPresenter.getComic(), list, pos);
                startActivity(readerIntent);
                break;
            case Task.STATE_PAUSE:
            case Task.STATE_ERROR:
                task.setState(Task.STATE_WAIT);
                mTaskAdapter.notifyItemChanged(position);
                Intent taskIntent = DownloadService.createIntent(this, task);
                startService(taskIntent);
                break;
            case Task.STATE_DOING:
            case Task.STATE_WAIT:
            case Task.STATE_PARSE:
                mBinder.getService().removeDownload(task.getId());
                task.setState(Task.STATE_PAUSE);
                mTaskAdapter.notifyItemChanged(task);
                break;
        }
    }

    @Override
    public void onItemLongClick(View view, final int position) {
        DialogUtils.buildPositiveDialog(TaskActivity.this, R.string.dialog_confirm, R.string.task_delete_confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.show();
                        Task task = mTaskAdapter.getItem(position);
                        mPresenter.deleteTask(task, mTaskAdapter.getItemCount() == 1);
                        mTaskAdapter.remove(position);
                    }
                }).show();
    }

    @OnClick(R.id.task_launch_btn) void onLaunchClick() {
        Comic comic = mPresenter.getComic();
        Intent intent = DetailActivity.createIntent(this, comic.getId(), comic.getSource(), comic.getCid());
        startActivity(intent);
    }

    @Override
    public void onTaskLoadSuccess(final List<Task> list) {
        mTaskAdapter.addAll(list);
        mPresenter.sortTask(list);
    }

    @Override
    public void onTaskLoadFail() {
        mProgressBar.setVisibility(View.GONE);
        showSnackbar(R.string.task_load_task_fail);
    }

    @Override
    public void onSortSuccess(final List<Task> list) {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = (DownloadServiceBinder) service;
                mBinder.getService().initTask(mTaskAdapter.getDateSet());
                mTaskAdapter.setData(list);
                mRecyclerView.setAdapter(mTaskAdapter);
                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        bindService(new Intent(this, DownloadService.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onLoadIndexFail() {
        mProgressBar.setVisibility(View.GONE);
        showSnackbar(R.string.task_load_index_fail);
    }

    @Override
    public void onTaskAdd(List<Task> list) {
        mTaskAdapter.addAll(0, list);
    }

    @Override
    public void onTaskDeleteSuccess() {
        mProgressDialog.hide();
        showSnackbar(R.string.task_delete_success);
    }

    @Override
    public void onTaskDeleteFail() {
        mProgressDialog.hide();
        showSnackbar(R.string.task_delete_fail);
    }

    @Override
    public void onTaskError(long id) {
        int position = mTaskAdapter.getPositionById(id);
        if (position != -1) {
            Task task = mTaskAdapter.getItem(position);
            if (task.getState() != Task.STATE_PAUSE) {
                mTaskAdapter.getItem(position).setState(Task.STATE_ERROR);
                notifyItemChanged(position);
            }
        }
    }

    @Override
    public void onTaskDoing(long id, int max) {
        int position = mTaskAdapter.getPositionById(id);
        if (position != -1) {
            Task task = mTaskAdapter.getItem(position);
            task.setMax(max);
            task.setState(Task.STATE_DOING);
            notifyItemChanged(position);
        }
    }

    @Override
    public void onTaskFinish(long id) {
        int position = mTaskAdapter.getPositionById(id);
        if (position != -1) {
            Task task = mTaskAdapter.getItem(position);
            task.setProgress(task.getMax());
            task.setState(Task.STATE_FINISH);
            notifyItemChanged(position);
        }
    }

    @Override
    public void onTaskParse(long id) {
        int position = mTaskAdapter.getPositionById(id);
        if (position != -1) {
            mTaskAdapter.getItem(position).setState(Task.STATE_PARSE);
            notifyItemChanged(position);
        }
    }

    @Override
    public void onTaskProcess(long id, int progress, int max) {
        int position = mTaskAdapter.getPositionById(id);
        if (position != -1) {
            Task task = mTaskAdapter.getItem(position);
            task.setMax(max);
            task.setProgress(progress);
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

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_task;
    }

    @Override
    protected View getLayoutView() {
        return mTaskLayout;
    }

    public static final String EXTRA_KEY = "a";

    public static Intent createIntent(Context context, Long id) {
        Intent intent = new Intent(context, TaskActivity.class);
        intent.putExtra(EXTRA_KEY, id);
        return intent;
    }

}
