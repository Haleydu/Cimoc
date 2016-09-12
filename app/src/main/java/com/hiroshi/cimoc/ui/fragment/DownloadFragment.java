package com.hiroshi.cimoc.ui.fragment;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.presenter.DownloadPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.activity.TaskActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;
import com.hiroshi.cimoc.ui.view.DownloadView;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadFragment extends BaseFragment implements DownloadView, BaseAdapter.OnItemClickListener {

    @BindView(R.id.download_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.download_control_btn) FloatingActionButton mControlBtn;

    private ComicAdapter mComicAdapter;
    private DownloadPresenter mPresenter;

    private boolean start;

    @Override
    protected void initPresenter() {
        mPresenter = new DownloadPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        mComicAdapter = new ComicAdapter(getActivity(), new LinkedList<MiniComic>());
        mComicAdapter.setOnItemClickListener(this);
        mComicAdapter.setProvider(new ControllerBuilderProvider(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.addItemDecoration(mComicAdapter.getItemDecoration());
        mRecyclerView.setAdapter(mComicAdapter);
    }

    @Override
    protected void initData() {
        start = false;
        mPresenter.loadComic();
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onItemClick(View view, int position) {
        MiniComic comic = mComicAdapter.getItem(position);
        Intent intent =
                TaskActivity.createIntent(getActivity(), comic.getId(), comic.getSource(), comic.getCid(), comic.getTitle());
        startActivity(intent);
    }

    @OnClick(R.id.download_control_btn) void onControlBtnClick() {
        if (start) {
            showProgressDialog();
            getActivity().stopService(new Intent(getActivity(), DownloadService.class));
            onDownloadStop();
            showSnackbar(R.string.download_stop_success);
            hideProgressDialog();
        } else {
            mPresenter.loadTask();
        }
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        mComicAdapter.addAll(list);
    }

    @Override
    public void onComicLoadFail() {
        mControlBtn.setVisibility(View.GONE);
        showSnackbar(R.string.download_load_comic_fail);
    }

    @Override
    public void onTaskLoadFail() {
        showSnackbar(R.string.download_task_fail);
        hideProgressDialog();
    }

    @Override
    public void onTaskLoadSuccess(List<Task> list) {
        if (list.isEmpty()) {
            showSnackbar(R.string.download_task_empty);
        } else {
            for (Task task : list) {
                MiniComic comic = mComicAdapter.getItemById(task.getKey());
                if (comic != null) {
                    task.setInfo(comic.getSource(), comic.getCid(), comic.getTitle());
                }
                task.setState(Task.STATE_WAIT);
                Intent intent = DownloadService.createIntent(getActivity(), task);
                getActivity().startService(intent);
            }
            showSnackbar(R.string.download_start_success);
        }
        hideProgressDialog();
    }

    @Override
    public void onDownloadAdd(MiniComic comic) {
        if (!mComicAdapter.exist(comic)) {
            mComicAdapter.add(0, comic);
        }
    }

    @Override
    public void onDownloadDelete(long id) {
        mComicAdapter.removeById(id);
    }

    @Override
    public void onDownloadStart() {
        if (!start) {
            start = true;
            mControlBtn.setImageResource(R.drawable.ic_pause_white_24dp);
        }
    }

    @Override
    public void onDownloadStop() {
        if (start) {
            start = false;
            mControlBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        }
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_download;
    }

}
