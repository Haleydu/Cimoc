package com.hiroshi.cimoc.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.widget.LinearLayoutManager;
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
import com.hiroshi.cimoc.presenter.TaskPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.service.DownloadService.DownloadServiceBinder;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.TaskAdapter;
import com.hiroshi.cimoc.ui.fragment.ComicFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.SelectDialogFragment;
import com.hiroshi.cimoc.ui.view.TaskView;
import com.hiroshi.cimoc.utils.ThemeUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public class TaskActivity extends BackActivity implements TaskView, BaseAdapter.OnItemClickListener,
        BaseAdapter.OnItemLongClickListener, MessageDialogFragment.MessageDialogListener,
        SelectDialogFragment.SelectDialogListener {

    @BindView(R.id.task_layout) View mTaskLayout;
    @BindView(R.id.task_recycler_view) RecyclerView mRecyclerView;

    private TaskAdapter mTaskAdapter;
    private TaskPresenter mPresenter;
    private ServiceConnection mConnection;
    private DownloadServiceBinder mBinder;
    private List<Task> mTempList;

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
        mTempList = new LinkedList<>();
        long key = getIntent().getLongExtra(EXTRA_KEY, -1);
        mPresenter.load(key);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
        if (mConnection != null) {
            unbindService(mConnection);
            mConnection = null;
            mBinder = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.task_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @OnClick(R.id.task_launch_btn) void onLaunchClick() {
        Comic comic = mPresenter.getComic();
        Intent intent = DetailActivity.createIntent(this, comic.getId());
        startActivity(intent);
    }

    @Override
    public void onChapterChange(String last) {
        mTaskAdapter.setLast(mPresenter.getComic().getLast());
    }

    @Override
    public void onItemClick(View view, final int position) {
        Task task = mTaskAdapter.getItem(position);
        switch (task.getState()) {
            case Task.STATE_FINISH:
                final String last = mTaskAdapter.getItem(position).getPath();
                Observable.from(mTaskAdapter.getDateSet())
                        .filter(new Func1<Task, Boolean>() {
                            @Override
                            public Boolean call(Task task) {
                                return task.getState() == Task.STATE_FINISH;
                            }
                        })
                        .map(new Func1<Task, Chapter>() {
                            @Override
                            public Chapter call(Task task) {
                                return new Chapter(task.getTitle(), task.getPath(), task.getMax(), true);
                            }
                        })
                        .toList()
                        .subscribe(new Action1<List<Chapter>>() {
                            @Override
                            public void call(final List<Chapter> list) {
                                for (Chapter chapter : list) {
                                    if (chapter.getPath().equals(last)) {
                                        mTaskAdapter.setLast(last);
                                        long id = mPresenter.updateLast(last);
                                        int mode = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
                                        Intent readerIntent = ReaderActivity.createIntent(TaskActivity.this, mode, list);
                                        startActivity(readerIntent);
                                        break;
                                    }
                                }
                            }
                        });
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

    /**
     *  delete task
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.task_delete_multi:
                ArrayList<Selectable> list = new ArrayList<>(mTaskAdapter.getItemCount());
                for (Task task : mTaskAdapter.getDateSet()) {
                    list.add(new Selectable(false, false, task.getTitle()));
                }
                SelectDialogFragment fragment = SelectDialogFragment.newInstance(list, R.string.task_delete_multi);
                fragment.show(getFragmentManager(), null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSelectPositiveClick(int type, List<Selectable> list) {
        for (int i = 0; i != list.size(); ++i) {
            if (list.get(i).isChecked()) {
                mTempList.add(mTaskAdapter.getItem(i));
            }
        }
        deleteTask();
    }

    @Override
    public void onSelectNeutralClick(int type, List<Selectable> list) {
        mTempList = mTaskAdapter.getDateSet();
        deleteTask();
    }

    @Override
    public void onItemLongClick(View view, int position) {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.task_delete_confirm, true);
        Task task = mTaskAdapter.getItem(position);
        mTempList.add(task);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onMessagePositiveClick(int type) {
        deleteTask();
    }

    private void deleteTask() {
        if (!mTempList.isEmpty()) {
            showProgressDialog();
            for (Task task : mTempList) {
                mBinder.getService().removeDownload(task.getId());
            }
            mPresenter.deleteTask(mTempList, mTaskAdapter.getItemCount() == mTempList.size());
        }
    }

    @Override
    public void onTaskDeleteSuccess() {
        mTaskAdapter.removeAll(mTempList);
        mTempList.clear();
        hideProgressDialog();
        showSnackbar(R.string.common_delete_success);
    }

    @Override
    public void onTaskDeleteFail() {
        mTempList.clear();
        hideProgressDialog();
        showSnackbar(R.string.common_delete_fail);
    }

    /**
     *  init: load task -> sort task
     */

    @Override
    public void onTaskLoadSuccess(final List<Task> list) {
        mTaskAdapter.setColorId(ThemeUtils.getResourceId(this, R.attr.colorAccent));
        mTaskAdapter.setLast(mPresenter.getComic().getLast());
        mTaskAdapter.addAll(list);
        mPresenter.sortTask(list);
    }

    @Override
    public void onTaskLoadFail() {
        hideProgressBar();
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
        showSnackbar(R.string.task_load_index_fail);
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
                mTaskAdapter.getItem(position).setState(Task.STATE_ERROR);
                notifyItemChanged(position);
            }
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
            int state = max == progress ? Task.STATE_FINISH : Task.STATE_DOING;
            task.setState(state);
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
