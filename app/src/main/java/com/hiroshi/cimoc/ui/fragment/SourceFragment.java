package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Card;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.SourcePresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.ui.adapter.CardAdapter;
import com.hiroshi.cimoc.ui.view.SourceView;
import com.hiroshi.cimoc.utils.DialogUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceFragment extends CardFragment implements SourceView {

    private SourcePresenter mPresenter;
    private CardAdapter<Source> mCardAdapter;

    @Override
    protected void initPresenter() {
        mPresenter = new SourcePresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mCardAdapter = new CardAdapter<>(getActivity(), new LinkedList<Source>());
        mCardAdapter.setOnItemLongClickListener(this);
        mCardAdapter.setOnItemClickListener(this);
        mCardAdapter.setOnItemCheckedListener(this);
        mRecyclerView.addItemDecoration(mCardAdapter.getItemDecoration());
        mRecyclerView.setAdapter(mCardAdapter);
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = ResultActivity.createIntent(getActivity(), mCardAdapter.getItem(position).getType());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, final int position) {
        DialogUtils.buildPositiveDialog(getActivity(), R.string.dialog_confirm, R.string.source_delete_confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.show();
                        Card card = mCardAdapter.getItem(position);
                        mPresenter.delete(card.getId(), card.getType(), position);
                    }
                }).show();
    }

    @Override
    public void onItemCheckedListener(boolean isChecked, int position) {
        Source source = mCardAdapter.getItem(position);
        source.setEnable(isChecked);
        mPresenter.update(source);
    }

    @Override
    protected void onActionConfirm(String text) {
        Source source = SourceManager.getSource(text);
        if (source == null) {
            showSnackbar(R.string.source_add_error);
        } else if (mCardAdapter.contain(source.getType())) {
            showSnackbar(R.string.source_add_exist);
        } else {
            mPresenter.insert(source);
        }
    }

    @Override
    protected int getActionTitle() {
        return R.string.source_add;
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroyView();
        mCardAdapter = null;
    }

    @Override
    public void onSourceAdd(Source source) {
        mCardAdapter.add(source);
        showSnackbar(R.string.card_add_success);
    }

    @Override
    public void onSourceLoadSuccess(List<Source> list) {
        mCardAdapter.addAll(list);
        onInitSuccess();
    }

    @Override
    public void onSourceDeleteSuccess(int position) {
        mProgressDialog.hide();
        mCardAdapter.remove(position);
        showSnackbar(R.string.source_delete_success);
    }

    @Override
    public void onSourceDeleteFail() {
        mProgressDialog.hide();
        showSnackbar(R.string.source_delete_fail);
    }

}
