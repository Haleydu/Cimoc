package com.hiroshi.cimoc.ui.fragment.coordinator.grid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.HistoryPresenter;
import com.hiroshi.cimoc.ui.activity.DetailActivity;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.HistoryView;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class HistoryFragment extends GridFragment implements HistoryView {

    private static final int DIALOG_REQUEST_DELETE = 0;
    private static final int DIALOG_REQUEST_CLEAR = 1;

    private HistoryPresenter mPresenter;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new HistoryPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initData() {
        mPresenter.load();
    }

    @Override
    public void onItemClick(View view, int position) {
        MiniComic comic = mGridAdapter.getItem(position);
        Intent intent = DetailActivity.createIntent(getActivity(), comic.getId(), -1, null, false);
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_DIALOG_BUNDLE_ARG_1, position);
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.history_delete_confirm, true, bundle, DIALOG_REQUEST_DELETE);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_DELETE:
                int pos = bundle.getBundle(EXTRA_DIALOG_BUNDLE).getInt(EXTRA_DIALOG_BUNDLE_ARG_1);
                mPresenter.delete(mGridAdapter.getItem(pos));
                mGridAdapter.remove(pos);
                showSnackbar(R.string.common_execute_success);
                break;
            case DIALOG_REQUEST_CLEAR:
                showProgressDialog();
                mPresenter.clear();
                break;
        }
    }

    @OnClick(R.id.coordinator_action_button) void onActionButtonClick() {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.history_clear_confirm, true, null, DIALOG_REQUEST_CLEAR);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onHistoryClearSuccess() {
        mGridAdapter.clear();
        showSnackbar(R.string.common_execute_success);
        hideProgressDialog();
    }

    @Override
    public void onHistoryClearFail() {
        showSnackbar(R.string.common_execute_fail);
        hideProgressDialog();
    }

    @Override
    public void onItemUpdate(MiniComic comic) {
        mGridAdapter.remove(comic);
        mGridAdapter.add(0, comic);
    }

    @Override
    protected int getImageRes() {
        return R.drawable.ic_delete_white_24dp;
    }

}
