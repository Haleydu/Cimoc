package com.hiroshi.cimoc.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.TaskPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.service.DownloadService.DownloadServiceBinder;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.TaskAdapter;
import com.hiroshi.cimoc.ui.view.TaskView;
import com.hiroshi.cimoc.utils.DialogUtils;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public class TaskActivity extends BaseActivity implements TaskView, BaseAdapter.OnItemClickListener, BaseAdapter.OnItemLongClickListener {

    @BindView(R.id.task_recycler_view) RecyclerView mRecyclerView;

    private TaskAdapter mTaskAdapter;
    private TaskPresenter mPresenter;
    private ServiceConnection mConnection;
    private DownloadServiceBinder mBinder;

    private int source;
    private String cid;
    private String comic;

    @Override
    protected void initView() {
        mTaskAdapter = new TaskAdapter(this, new LinkedList<Task>());
        mTaskAdapter.setOnItemClickListener(this);
        mTaskAdapter.setOnItemLongClickListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(mTaskAdapter.getItemDecoration());
    }

    @Override
    public void onItemClick(View view, int position) {
        Task task = mTaskAdapter.getItem(position);
        switch (task.getState()) {
            case Task.STATE_FINISH:
                int pos = 0;
                List<Chapter> list = new LinkedList<>();
                List<Task> dataSet = mTaskAdapter.getDateSet();
                for (int i = 0; i != dataSet.size(); ++i) {
                    Task temp = dataSet.get(i);
                    if (temp.getState() == Task.STATE_FINISH) {
                        list.add(new Chapter(temp.getTitle(), temp.getPath(), temp.getMax(), true));
                        if (temp.equals(task)) {
                            pos = list.size() - 1;
                        }
                    }
                }
                Intent readerIntent = ReaderActivity.createIntent(this, source, cid, comic, list, pos);
                startActivity(readerIntent);
                break;
            case Task.STATE_PAUSE:
            case Task.STATE_ERROR:
                task.setInfo(source, cid, comic);
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
                        mPresenter.deleteTask(mTaskAdapter.getItem(position), mTaskAdapter.getItemCount() == 1);
                        mTaskAdapter.remove(position);
                    }
                }).show();
    }

    @Override
    protected void initToolbar() {
        super.initToolbar();
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void initData() {
        source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        cid = getIntent().getStringExtra(EXTRA_CID);
        comic = getIntent().getStringExtra(EXTRA_COMIC);
        long key = getIntent().getLongExtra(EXTRA_KEY, -1);
        mPresenter.loadTask(key);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new TaskPresenter();
        mPresenter.attachView(this);
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
    protected String getDefaultTitle() {
        return getString(R.string.task_list);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_task;
    }

    @Override
    public void onLoadSuccess(final List<Task> list) {
        mPresenter.sortTask(list, source, comic);
    }

    @Override
    public void onSortSuccess(final List<Task> list) {
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = (DownloadServiceBinder) service;
                mBinder.getService().initTask(list);
                mTaskAdapter.addAll(list);
                mRecyclerView.setAdapter(mTaskAdapter);
                hideProgressBar();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        bindService(new Intent(this, DownloadService.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onTaskError(long id) {
        Task task = mTaskAdapter.getItemById(id);
        if (task != null) {
            task.setState(Task.STATE_ERROR);
            notifyItemChanged(task);
        }
    }

    @Override
    public void onTaskDoing(long id, int max) {
        Task task = mTaskAdapter.getItemById(id);
        if (task != null) {
            task.setMax(max);
            task.setState(Task.STATE_DOING);
            notifyItemChanged(task);
        }
    }

    @Override
    public void onTaskFinish(long id) {
        Task task = mTaskAdapter.getItemById(id);
        if (task != null) {
            task.setState(Task.STATE_FINISH);
            notifyItemChanged(task);
        }
    }

    @Override
    public void onTaskParse(long id) {
        Task task = mTaskAdapter.getItemById(id);
        if (task != null) {
            task.setState(Task.STATE_PARSE);
            notifyItemChanged(task);
        }
    }

    @Override
    public void onTaskProcess(long id, int progress, int max) {
        Task task = mTaskAdapter.getItemById(id);
        if (task != null) {
            task.setProgress(progress);
            task.setMax(max);
            notifyItemChanged(task);
        }
    }

    private void notifyItemChanged(Task task) {
        if (!mRecyclerView.isComputingLayout()) {
            mTaskAdapter.notifyItemChanged(task);
        }
    }

    public static final String EXTRA_KEY = "a";
    public static final String EXTRA_SOURCE = "b";
    public static final String EXTRA_CID = "c";
    public static final String EXTRA_COMIC = "d";

    public static Intent createIntent(Context context, Long id, int source, String cid, String comic) {
        Intent intent = new Intent(context, TaskActivity.class);
        intent.putExtra(EXTRA_KEY, id);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_CID, cid);
        intent.putExtra(EXTRA_COMIC, comic);
        return intent;
    }

}
