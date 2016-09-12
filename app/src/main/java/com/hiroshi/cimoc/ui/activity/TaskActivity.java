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

    private long key;
    private int source;
    private String cid;
    private String comic;

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
        key = getIntent().getLongExtra(EXTRA_KEY, -1);
        source = getIntent().getIntExtra(EXTRA_SOURCE, -1);
        cid = getIntent().getStringExtra(EXTRA_CID);
        comic = getIntent().getStringExtra(EXTRA_COMIC);
        mPresenter.loadTask(key);
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
                String[] title = mTaskAdapter.getTaskTitle();
                final boolean[] array = new boolean[title.length];
                DialogUtils.buildMultiChoiceDialog(this, R.string.task_delete_multi, title, array,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                array[which] = isChecked;
                            }
                        }, R.string.task_delete_all, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mProgressDialog.show();
                                mPresenter.deleteTask(new LinkedList<>(mTaskAdapter.getDateSet()), source, comic, key, true);
                                mTaskAdapter.clear();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mProgressDialog.show();
                                List<Task> list = new LinkedList<>();
                                for (int i = 0; i != array.length; ++i) {
                                    if (array[i]) {
                                        list.add(mTaskAdapter.getItem(i));
                                    }
                                }
                                mPresenter.deleteTask(list, source, comic, key, mTaskAdapter.getItemCount() == list.size());
                                mTaskAdapter.removeAll(list);
                            }
                        }).show();
                break;
        }
        return super.onOptionsItemSelected(item);
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
        Intent intent = DetailActivity.createIntent(this, key, source, cid);
        startActivity(intent);
    }

    @Override
    public void onLoadSuccess(final List<Task> list) {
        for (Task task : list) {
            task.setInfo(source, cid, comic);
        }
        mTaskAdapter.addAll(list);
        mPresenter.sortTask(list, source, comic);
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
                hideProgressBar();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {}
        };
        bindService(new Intent(this, DownloadService.class), mConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void onLoadIndexFail() {
        hideProgressBar();
        showSnackbar(R.string.task_load_fail);
    }

    @Override
    public void onTaskAdd(Task task) {
        if (task.getSource() == source && task.getComic().equals(comic)) {
            mTaskAdapter.add(0, task);
        }
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
            task.setProgress(progress);
            task.setMax(max);
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
