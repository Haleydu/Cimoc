package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.SourcePresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.SourceAdapter;
import com.hiroshi.cimoc.ui.view.SourceView;
import com.hiroshi.cimoc.utils.DialogUtils;

import java.util.LinkedList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceFragment extends BaseFragment implements SourceView,
        BaseAdapter.OnItemClickListener, BaseAdapter.OnItemLongClickListener, SourceAdapter.OnItemCheckedListener {

    @BindView(R.id.source_recycler_view) RecyclerView mRecyclerView;

    protected AlertDialog mProgressDialog;
    private SourceAdapter mSourceAdapter;
    private SourcePresenter mPresenter;

    @Override
    protected void initView() {
        mProgressDialog = DialogUtils.buildCancelableFalseDialog(getActivity(), R.string.dialog_doing);
        mSourceAdapter = new SourceAdapter(getActivity(), new LinkedList<Source>());
        mSourceAdapter.setOnItemLongClickListener(this);
        mSourceAdapter.setOnItemClickListener(this);
        mSourceAdapter.setOnItemCheckedListener(this);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        mRecyclerView.setAdapter(mSourceAdapter);
        mRecyclerView.addItemDecoration(mSourceAdapter.getItemDecoration());
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = ResultActivity.createIntent(getActivity(), mSourceAdapter.getItem(position).getSid());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, final int position) {
        DialogUtils.buildPositiveDialog(getActivity(), R.string.dialog_confirm, R.string.source_delete_confirm,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.show();
                        mPresenter.delete(mSourceAdapter.getItem(position), position);
                    }
                }).show();
    }

    @Override
    public void onItemCheckedListener(boolean isChecked, int position) {
        Source source = mSourceAdapter.getItem(position);
        source.setEnable(isChecked);
        mPresenter.update(source);
    }

    @OnClick(R.id.source_add_btn) void onSourceAddClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_source, null);
        final EditText editText = (EditText) view.findViewById(R.id.source_edit_text);
        builder.setTitle(R.string.source_add);
        builder.setView(view);
        builder.setPositiveButton(R.string.dialog_positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int sid = SourceManager.getId(editText.getText().toString());
                if (sid == -1) {
                    showSnackbar(R.string.source_add_error);
                } else if (mSourceAdapter.contain(sid)) {
                    showSnackbar(R.string.source_add_exist);
                } else {
                    mPresenter.add(sid);
                }
            }
        });
        builder.show();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroyView();
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        mSourceAdapter = null;
    }

    @Override
    protected void initPresenter() {
        mPresenter = new SourcePresenter();
        mPresenter.attachView(this);
    }

    @Override
    public void onSourceAdd(Source source) {
        mSourceAdapter.add(source);
        showSnackbar(R.string.source_add_success);
    }

    @Override
    public void onSourceLoadSuccess(List<Source> list) {
        mSourceAdapter.addAll(list);
        onInitSuccess();
    }

    @Override
    public void onSourceLoadFail() {
        showSnackbar(R.string.source_load_fail);
        onInitSuccess();
    }

    @Override
    public void onSourceDeleteSuccess(int position) {
        mProgressDialog.hide();
        mSourceAdapter.remove(position);
        showSnackbar(R.string.source_delete_success);
    }

    @Override
    public void onSourceDeleteFail() {
        mProgressDialog.hide();
        showSnackbar(R.string.source_delete_fail);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_source;
    }

}
