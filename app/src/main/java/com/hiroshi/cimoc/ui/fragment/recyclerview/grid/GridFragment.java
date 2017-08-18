package com.hiroshi.cimoc.ui.fragment.recyclerview.grid;

import android.content.Intent;
import android.support.annotation.ColorRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.activity.TaskActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.ItemDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.fragment.recyclerview.RecyclerViewFragment;
import com.hiroshi.cimoc.ui.view.GridView;
import com.hiroshi.cimoc.utils.HintUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/22.
 */

public abstract class GridFragment extends RecyclerViewFragment implements GridView {

    protected static final int DIALOG_REQUEST_OPERATION = 0;

    @BindView(R.id.grid_action_button) FloatingActionButton mActionButton;

    protected GridAdapter mGridAdapter;

    protected long mSavedId = -1;

    @Override
    protected BaseAdapter initAdapter() {
        mGridAdapter = new GridAdapter(getActivity(), new LinkedList<MiniComic>());
        mGridAdapter.setProvider(getAppInstance().getBuilderProvider());
        mGridAdapter.setTitleGetter(SourceManager.getInstance(this).new TitleGetter());
        mRecyclerView.setRecycledViewPool(getAppInstance().getGridRecycledPool());
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                switch (newState){
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        getAppInstance().getBuilderProvider().pause();
                        break;
                    case RecyclerView.SCROLL_STATE_IDLE:
                        getAppInstance().getBuilderProvider().resume();
                        break;
                }
            }
        });
        mActionButton.setImageResource(getActionButtonRes());
        return mGridAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager initLayoutManager() {
        GridLayoutManager manager = new GridLayoutManager(getActivity(), 3);
        manager.setRecycleChildrenOnDetach(true);
        return manager;
    }

    @OnClick(R.id.grid_action_button) void onActionButtonClick() {
        performActionButtonClick();
    }

    @Override
    public void onItemClick(View view, int position) {
        MiniComic comic = mGridAdapter.getItem(position);
        Intent intent = comic.isLocal() ? TaskActivity.createIntent(getActivity(), comic.getId()) :
                DetailActivity.createIntent(getActivity(), comic.getId(), -1, null);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        mSavedId = mGridAdapter.getItem(position).getId();
        ItemDialogFragment fragment = ItemDialogFragment.newInstance(R.string.common_operation_select,
                getOperationItems(), DIALOG_REQUEST_OPERATION);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        mGridAdapter.addAll(list);
    }

    @Override
    public void onComicLoadFail() {
        HintUtils.showToast(getActivity(), R.string.common_data_load_fail);
    }

    @Override
    public void onExecuteFail() {
        hideProgressDialog();
        HintUtils.showToast(getActivity(), R.string.common_execute_fail);
    }

    @Override
    public void onThemeChange(@ColorRes int primary, @ColorRes int accent) {
        mActionButton.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), accent));
    }

    protected void showComicInfo(Comic comic, int request) {
        if (comic == null) {
            MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.common_execute_fail,
                    R.string.comic_info_not_found, true, request);
            fragment.setTargetFragment(this, 0);
            fragment.show(getFragmentManager(), null);
            return;
        }
        String content =
                StringUtils.format("%s  %s\n%s  %s\n%s  %s\n%s  %s\n%s  %s",
                getString(R.string.comic_info_title),
                comic.getTitle(),
                getString(R.string.comic_info_source),
                SourceManager.getInstance(this).getParser(comic.getSource()).getTitle(),
                getString(R.string.comic_info_status),
                comic.getFinish() == null ? getString(R.string.comic_status_finish) :
                        getString(R.string.comic_status_continue),
                getString(R.string.comic_info_chapter),
                comic.getChapter() == null ? getString(R.string.common_null) : comic.getChapter(),
                getString(R.string.comic_info_time),
                comic.getHistory() == null ? getString(R.string.common_null) :
                        StringUtils.getFormatTime("yyyy-MM-dd HH:mm:ss", comic.getHistory()));
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.comic_info,
                content, true, request);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    protected abstract void performActionButtonClick();

    protected abstract int getActionButtonRes();

    protected abstract String[] getOperationItems();

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_grid;
    }

}
