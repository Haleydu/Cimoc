package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;
import com.hiroshi.cimoc.ui.view.GridView;
import com.hiroshi.cimoc.utils.DialogUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/22.
 */

public abstract class GridFragment extends BaseFragment implements GridView, BaseAdapter.OnItemClickListener {

    @BindView(R.id.grid_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.grid_action_button) FloatingActionButton mActionButton;

    protected AlertDialog mProgressDialog;
    protected ControllerBuilderProvider mBuilderProvider;
    protected ComicAdapter mComicAdapter;

    @Override
    protected void initView() {
        mProgressDialog = DialogUtils.buildCancelableFalseDialog(getActivity(), R.string.dialog_doing);
        mBuilderProvider = new ControllerBuilderProvider(getActivity());
        mActionButton.setImageResource(getImageRes());
        initAdapter();
        mComicAdapter.setOnItemClickListener(this);
        mComicAdapter.setProvider(mBuilderProvider);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.addItemDecoration(mComicAdapter.getItemDecoration());
        mRecyclerView.setAdapter(mComicAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mBuilderProvider != null) {
            mBuilderProvider.clear();
            mBuilderProvider = null;
        }
        mComicAdapter = null;
    }

    @OnClick(R.id.grid_action_button) void onClick() {
        DialogUtils.buildPositiveDialog(getActivity(), R.string.dialog_confirm, getActionRes(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onActionConfirm();
                    }
                }).show();
    }

    @Override
    public void onComicLoadSuccess(List<MiniComic> list) {
        mComicAdapter.addAll(list);
        onInitSuccess();
    }

    @Override
    public void onComicLoadFail() {
        showSnackbar(R.string.cimoc_load_fail);
        onInitSuccess();
    }

    protected abstract int getActionRes();

    protected abstract void onActionConfirm();

    protected abstract int getImageRes();

    protected abstract void initAdapter();

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_grid;
    }

}
