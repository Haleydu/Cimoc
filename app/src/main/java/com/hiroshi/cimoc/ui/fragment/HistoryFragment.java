package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.HistoryPresenter;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;
import com.hiroshi.cimoc.ui.view.HistoryView;
import com.hiroshi.cimoc.utils.DialogUtils;

import java.util.LinkedList;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class HistoryFragment extends GridFragment implements HistoryView {

    private HistoryPresenter mPresenter;

    @Override
    protected void initPresenter() {
        mPresenter = new HistoryPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initAdapter() {
        mComicAdapter = new ComicAdapter(getActivity(), new LinkedList<MiniComic>());
        mComicAdapter.setOnItemLongClickListener(new BaseAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                DialogUtils.buildPositiveDialog(getActivity(), R.string.dialog_confirm, R.string.history_delete_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPresenter.deleteHistory(mComicAdapter.getItem(position));
                                mComicAdapter.remove(position);
                            }
                        }).show();
            }
        });
    }

    @Override
    protected void initData() {
        mPresenter.loadComic();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroyView();
    }

    @Override
    protected void onActionConfirm() {
        mProgressDialog.show();
        mPresenter.clearHistory();
    }

    @Override
    public void onItemClick(View view, int position) {
        MiniComic comic = mComicAdapter.getItem(position);
        Intent intent = DetailActivity.createIntent(getActivity(), comic.getId(), comic.getSource(), comic.getCid());
        startActivity(intent);
    }

    @Override
    public void onHistoryClearSuccess() {
        int count = mComicAdapter.getItemCount();
        mComicAdapter.clear();
        showSnackbar(R.string.history_clear_success, count);
        mProgressDialog.hide();
    }

    @Override
    public void onHistoryClearFail() {
        showSnackbar(R.string.history_clear_fail);
        mProgressDialog.hide();
    }

    @Override
    public void onItemUpdate(MiniComic comic) {
        mComicAdapter.update(comic);
    }

    @Override
    public void onSourceRemove(int source) {
        mComicAdapter.removeBySource(source);
    }

    @Override
    protected int getImageRes() {
        return R.drawable.ic_delete_white_24dp;
    }

    @Override
    protected int getActionRes() {
        return R.string.history_clear_confirm;
    }

}
