package com.hiroshi.cimoc.ui.fragment.classical;

import android.content.Intent;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.presenter.SourcePresenter;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.ui.adapter.BaseAdapter;
import com.hiroshi.cimoc.ui.adapter.SourceAdapter;
import com.hiroshi.cimoc.ui.fragment.dialog.EditorDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.SourceView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/8/11.
 */
public class SourceFragment extends ClassicalFragment implements SourceView, SourceAdapter.OnItemCheckedListener,
        MessageDialogFragment.MessageDialogListener, EditorDialogFragment.EditorDialogListener {

    private SourcePresenter mPresenter;
    private SourceAdapter mSourceAdapter;
    private int mTempPosition = -1;

    @Override
    protected void initPresenter() {
        mPresenter = new SourcePresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        mSourceAdapter = new SourceAdapter(getActivity(), new ArrayList<Source>());
        mSourceAdapter.setOnItemLongClickListener(this);
        mSourceAdapter.setOnItemCheckedListener(this);
        super.initView();

    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroyView();
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent intent = ResultActivity.createIntent(getActivity(), mSourceAdapter.getItem(position).getType());
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, final int position) {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.source_delete_confirm, true);
        mTempPosition = position;
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onMessagePositiveClick(int type) {
        Source source = mSourceAdapter.getItem(mTempPosition);
        mPresenter.delete(source.getId());
        mSourceAdapter.remove(mTempPosition);
        showSnackbar(R.string.common_delete_success);
    }

    @Override
    public void onItemCheckedListener(boolean isChecked, int position) {
        Source source = mSourceAdapter.getItem(position);
        source.setEnable(isChecked);
        mPresenter.update(source);
    }

    @Override
    protected void onActionButtonClick() {
        EditorDialogFragment fragment = EditorDialogFragment.newInstance(R.string.source_add);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onEditorPositiveClick(String text) {
        Source source = SourceManager.getSource(text);
        if (source == null) {
            showSnackbar(R.string.common_add_fail);
        } else if (mSourceAdapter.contain(source.getType())) {
            showSnackbar(R.string.source_add_exist);
        } else {
            mPresenter.insert(source);
            mSourceAdapter.add(source);
            showSnackbar(R.string.common_add_success);
        }
    }

    @Override
    public void onSourceLoadSuccess(List<Source> list) {
        mSourceAdapter.addAll(list);
    }

    @Override
    public void onSourceLoadFail() {
        showSnackbar(R.string.common_data_load_fail);
    }

    @Override
    protected int getImageRes() {
        return R.drawable.ic_add_white_24dp;
    }

    @Override
    protected BaseAdapter getAdapter() {
        return mSourceAdapter;
    }

    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new GridLayoutManager(getActivity(), 2);
    }

}
