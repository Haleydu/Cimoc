package com.hiroshi.cimoc.ui.fragment.recyclerview.grid;

import android.os.Bundle;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.HistoryPresenter;
import com.hiroshi.cimoc.ui.fragment.dialog.MessageDialogFragment;
import com.hiroshi.cimoc.ui.view.HistoryView;
import com.hiroshi.cimoc.utils.HintUtils;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class HistoryFragment extends GridFragment implements HistoryView {

    private static final int DIALOG_REQUEST_DELETE = 0;
    private static final int DIALOG_REQUEST_CLEAR = 1;

    private HistoryPresenter mPresenter;

    private MiniComic mSavedComic;

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
    protected void performActionButtonClick() {
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.history_clear_confirm, true, DIALOG_REQUEST_CLEAR);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onItemLongClick(View view, int position) {
        mSavedComic = mGridAdapter.getItem(position);
        MessageDialogFragment fragment = MessageDialogFragment.newInstance(R.string.dialog_confirm,
                R.string.history_delete_confirm, true, DIALOG_REQUEST_DELETE);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        switch (requestCode) {
            case DIALOG_REQUEST_DELETE:
                mPresenter.delete(mSavedComic);
                mGridAdapter.remove(mSavedComic);
                HintUtils.showToast(getActivity(), R.string.common_execute_success);
                break;
            case DIALOG_REQUEST_CLEAR:
                showProgressDialog();
                mPresenter.clear();
                break;
        }
    }

    @Override
    public void onHistoryClearSuccess() {
        mGridAdapter.clear();
        hideProgressDialog();
        HintUtils.showToast(getActivity(), R.string.common_execute_success);
    }

    @Override
    public void onItemUpdate(MiniComic comic) {
        mGridAdapter.remove(comic);
        mGridAdapter.add(0, comic);
    }

    @Override
    protected int getActionButtonRes() {
        return R.drawable.ic_delete_white_24dp;
    }

}
