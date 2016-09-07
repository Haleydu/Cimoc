package com.hiroshi.cimoc.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.TaskPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.TaskAdapter;
import com.hiroshi.cimoc.ui.view.TaskView;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public class TaskActivity extends BaseActivity implements TaskView {

    @BindView(R.id.task_recycler_view) RecyclerView mRecyclerView;

    private TaskAdapter mTaskAdapter;
    private TaskPresenter mPresenter;
    private ServiceConnection mConnection;
    private IBinder mBinder;

    private int source;
    private String cid;
    private String comic;

    @Override
    protected void initView() {
        mTaskAdapter = new TaskAdapter(this, new LinkedList<Task>());
        mTaskAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Task task = mTaskAdapter.getItem(position);
                switch (task.getState()) {
                    case Task.STATE_FINISH:
                        // Todo 阅读漫画
                        break;
                    case Task.STATE_PAUSE:
                        task.setInfo(source, cid, comic);
                        task.setState(Task.STATE_WAIT);
                        mTaskAdapter.notifyItemChanged(position);
                        Intent intent = DownloadService.createIntent(TaskActivity.this, task);
                        startService(intent);
                        break;
                    case Task.STATE_DOING:
                    case Task.STATE_WAIT:
                    case Task.STATE_PARSE:
                        // Todo 暂停下载
                        break;
                }
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mTaskAdapter);
        mRecyclerView.addItemDecoration(mTaskAdapter.getItemDecoration());

        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mBinder = service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        bindService(new Intent(this, DownloadService.class), mConnection, 0);
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
        mPresenter.load(key);
    }

    @Override
    protected void initPresenter() {
        mPresenter = new TaskPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.download_list);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_task;
    }

    @Override
    public void onLoadSuccess(List<Task> list) {
        mTaskAdapter.addAll(list);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onTaskAdd(Task task) {
        mTaskAdapter.add(task);
    }

    @Override
    public void onTaskDoing(long id, int max) {
        Task task = mTaskAdapter.getItemById(id);
        if (task != null) {
            task.setMax(max);
            task.setState(Task.STATE_DOING);
            mTaskAdapter.notifyItemChanged(task);
        }
    }

    @Override
    public void onTaskFinish(long id) {
        Task task = mTaskAdapter.getItemById(id);
        if (task != null) {
            task.setFinish(true);
            task.setState(Task.STATE_FINISH);
            mTaskAdapter.notifyItemChanged(task);
        }
    }

    @Override
    public void onTaskParse(long id) {
        Task task = mTaskAdapter.getItemById(id);
        if (task != null) {
            task.setState(Task.STATE_PARSE);
            mTaskAdapter.notifyItemChanged(task);
        }
    }

    @Override
    public void onTaskProcess(long id, int progress, int max) {
        Task task = mTaskAdapter.getItemById(id);
        if (task != null) {
            task.setProgress(progress);
            task.setMax(max);
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
