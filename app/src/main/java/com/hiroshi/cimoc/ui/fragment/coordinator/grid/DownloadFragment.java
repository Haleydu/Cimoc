package com.hiroshi.cimoc.ui.fragment.coordinator.grid;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.DownloadPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.activity.TaskActivity;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.DownloadView;

import java.util.ArrayList;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadFragment extends GridFragment implements DownloadView {

    private static final int TYPE_PAUSE = 1;
    private static final int TYPE_DELETE = 2;

    private DownloadPresenter mPresenter;
    private int mTempPosition = -1;

    private boolean start = false;

    @Override
    protected void initPresenter() {
        mPresenter = new DownloadPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initData() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (info.service.getClassName().equals(DownloadService.class.getName())) {
                onDownloadStart();
            }
        }
        mPresenter.loadComic();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroyView();
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.download_action_confirm, true, TYPE_PAUSE);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onMessagePositiveClick(int type) {
        switch (type) {
            case TYPE_PAUSE:
                if (start) {
                    getActivity().stopService(new Intent(getActivity(), DownloadService.class));
                    onDownloadStop();
                    showSnackbar(R.string.download_stop_success);
                } else {
                    showProgressDialog();
                    mPresenter.loadTask();
                }
                break;
            case TYPE_DELETE:
                if (start) {
                    showSnackbar(R.string.download_ask_stop);
                } else {
                    showProgressDialog();
                    mPresenter.deleteComic(mGridAdapter.getItem(mTempPosition).getId());
                }
                break;
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        MiniComic comic = mGridAdapter.getItem(position);
        Intent intent = TaskActivity.createIntent(getActivity(), comic.getId());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        mTempPosition = position;
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.download_delete_confirm, true, TYPE_DELETE);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onComicLoadFail() {
        super.onComicLoadFail();
        mActionButton.setVisibility(View.GONE);
    }

    @Override
    public void onTaskLoadFail() {
        hideProgressDialog();
        showSnackbar(R.string.download_task_fail);
    }

    @Override
    public void onTaskLoadSuccess(ArrayList<Task> list) {
        if (list.isEmpty()) {
            showSnackbar(R.string.download_task_empty);
        } else {
            for (Task task : list) {
                MiniComic comic = mGridAdapter.getItemById(task.getKey());
                if (comic != null) {
                    task.setInfo(comic.getSource(), comic.getCid(), comic.getTitle());
                }
                task.setState(Task.STATE_WAIT);
            }
            Intent intent = DownloadService.createIntent(getActivity(), list);
            getActivity().startService(intent);
            showSnackbar(R.string.download_start_success);
        }
        hideProgressDialog();
    }

    @Override
    public void onDownloadAdd(MiniComic comic) {
        if (!mGridAdapter.exist(comic)) {
            mGridAdapter.add(0, comic);
        }
    }

    @Override
    public void onDownloadDelete(long id) {
        mGridAdapter.removeItemById(id);
    }

    @Override
    public void onDownloadStart() {
        if (!start) {
            start = true;
            mActionButton.setImageResource(R.drawable.ic_pause_white_24dp);
        }
    }

    @Override
    public void onDownloadStop() {
        if (start) {
            start = false;
            mActionButton.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }
    }

    @Override
    public void onDownloadDeleteSuccess() {
        hideProgressDialog();
        mGridAdapter.remove(mTempPosition);
        showSnackbar(R.string.common_delete_success);
    }

    @Override
    public void onDownloadDeleteFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_delete_fail);
    }

    @Override
    protected int getImageRes() {
        return R.drawable.ic_play_arrow_white_24dp;
    }

}
