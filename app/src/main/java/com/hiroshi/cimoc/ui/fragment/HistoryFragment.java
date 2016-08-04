package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
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

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class HistoryFragment extends BaseFragment {

    @BindView(R.id.history_comic_list) RecyclerView mRecyclerView;

    private ComicAdapter mComicAdapter;
    private HistoryPresenter mPresenter;

    @Override
    protected void initView() {
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
                DialogFactory.buildPositiveDialog(getActivity(), "删除提示", "是否删除该历史纪录", new DialogInterface.OnClickListener() {
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

}
