package com.hiroshi.cimoc.ui.fragment;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.SettingsPresenter;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsFragment extends BaseFragment {

    private SettingsPresenter mPresenter;
    private AlertDialog mAlertDialog;

    @OnClick({ R.id.settings_backup_save_btn, R.id.settings_backup_restore_btn, R.id.settings_other_history_btn, R.id.settings_other_cache_btn})
    void onClick(View view) {
        switch (view.getId()) {
            case R.id.settings_backup_save_btn:
                mPresenter.backupComic();
                break;
            case R.id.settings_backup_restore_btn:
                mPresenter.restoreComic();
                break;
            case R.id.settings_other_cache_btn:
                mPresenter.cleanCache();
                break;
            case R.id.settings_other_history_btn:
                mPresenter.cleanHistory();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAlertDialog.dismiss();
    }

    @Override
    protected void initPresenter() {
        mPresenter = new SettingsPresenter(this);
    }

    @Override
    protected void initView() {
        mAlertDialog = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog_Alert).setCancelable(false).create();
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_settings;
    }

    public void showAlertDialog(String msg) {
        mAlertDialog.setMessage(msg);
        mAlertDialog.show();
    }

    public void hideAlertDialog() {
        mAlertDialog.hide();
    }

    public void showSnackbar(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
        }
    }

}
