package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;
import com.hiroshi.cimoc.ui.view.GridView;
import com.hiroshi.cimoc.utils.DialogUtils;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Hiroshi on 2016/9/22.
 */

public abstract class GridFragment extends ClassicalFragment implements GridView {

    protected AlertDialog mProgressDialog;
    protected ControllerBuilderProvider mBuilderProvider;
    protected ComicAdapter mComicAdapter;

    @Override
    protected void initView() {
        mProgressDialog = DialogUtils.buildCancelableFalseDialog(getActivity(), R.string.dialog_doing);
        mComicAdapter = new ComicAdapter(getActivity(), new LinkedList<MiniComic>());
        mBuilderProvider = ((CimocApplication) getActivity().getApplication()).getBuilderProvider();
        mComicAdapter.setProvider(mBuilderProvider);
        super.initView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        mComicAdapter = null;
    }

    @Override
    public void onItemLongClick(View view, int position) {}

    @Override
    protected void onActionButtonClick() {
        DialogUtils.buildPositiveDialog(getActivity(), R.string.dialog_confirm, getActionRes(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onActionConfirm();
                    }
                }).show();
    }

    @Override
    public void onComicLoadSuccess(Collection<MiniComic> list) {
        mComicAdapter.addAll(list);
    }

    @Override
    public void onComicLoadFail() {
        showSnackbar(R.string.common_data_load_fail);
    }

    @Override
    public void onComicFilterSuccess(Collection<MiniComic> list) {
        mComicAdapter.setData(list);
    }

    @Override
    public void onComicFilterFail() {
        showSnackbar(R.string.comic_filter_fail);
    }

    protected abstract int getActionRes();

    protected abstract void onActionConfirm();

    @Override
    protected BaseAdapter getAdapter() {
        return mComicAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(getActivity(), 3);
    }

}
