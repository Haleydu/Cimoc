package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.HistoryPresenter;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.ComicAdapter;
import com.hiroshi.cimoc.utils.DialogFactory;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class HistoryFragment extends BaseFragment {

    @BindView(R.id.history_comic_list) RecyclerView mRecyclerView;

    private ComicAdapter mComicAdapter;
    private HistoryPresenter mPresenter;
    private AlertDialog mProgressDialog;

    @Override
    protected void initView() {
        mProgressDialog = DialogFactory.buildCancelableFalseDialog(getActivity());
        mComicAdapter = new ComicAdapter(getActivity(), mPresenter.getComic());
        mComicAdapter.setOnItemClickListener(new BaseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                mPresenter.onItemClick(mComicAdapter.getItem(position));
            }
        });
        mComicAdapter.setOnItemLongClickListener(new BaseAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                DialogFactory.buildPositiveDialog(getActivity(), R.string.dialog_confirm, R.string.history_delete_confirm,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPresenter.onPositiveClick(mComicAdapter.getItem(position));
                                mComicAdapter.remove(position);
                            }
                        }).show();
            }
        });
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setAdapter(mComicAdapter);
        mRecyclerView.addItemDecoration(mComicAdapter.getItemDecoration());
    }

    @OnClick(R.id.history_clear_btn) void onHistoryClearClick() {
        DialogFactory.buildPositiveDialog(getActivity(), R.string.dialog_confirm, R.string.history_clear_confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.setMessage("正在删除...");
                        mProgressDialog.show();
                        mPresenter.onHistoryClearClick();
                    }
                }).show();
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new HistoryPresenter(this);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_history;
    }

    public void updateItem(MiniComic comic) {
        mComicAdapter.update(comic);
    }

    public void clearItem() {
        mComicAdapter.clear();
    }

    public void hideProgressDialog() {
        mProgressDialog.hide();
    }

}
