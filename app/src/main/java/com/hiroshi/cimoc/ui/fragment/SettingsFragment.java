package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.SettingsPresenter;
import com.hiroshi.cimoc.utils.DialogFactory;
import com.hiroshi.cimoc.utils.PreferenceMaster;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsFragment extends BaseFragment {

    @BindView(R.id.settings_other_home_summary) TextView mIndexSummary;

    private SettingsPresenter mPresenter;
    private AlertDialog mProgressDialog;

    private int mBackupChoice;
    private int mHomeChoice;

    @Override
    protected void initView() {
        mProgressDialog = new AlertDialog.Builder(getActivity(), R.style.AppTheme_Dialog_Alert).setCancelable(false).create();
        mHomeChoice = CimocApplication.getPreferences().getInt(PreferenceMaster.PREF_HOME, R.id.drawer_cimoc);
        mIndexSummary.setText(PreferenceMaster.getTitleById(mHomeChoice));
    }

    @OnClick(R.id.settings_backup_restore_btn) void onRestoreBtnClick() {
        final String[] array = mPresenter.getFiles();
        if (array == null) {
            showSnackbar("没有找到备份文件");
            return;
        }
        DialogFactory.buildSingleChoiceDialog(getActivity(), "选择文件", array, -1,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBackupChoice = which;
                    }
                },
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mPresenter.onRestorePositiveBtnClick(array[mBackupChoice]);
                    }
                }).show();
    }

    @OnClick(R.id.settings_other_home_btn) void onHomeBtnClick() {
        final int[] array = new int[] { R.id.drawer_cimoc, R.id.drawer_favorite, R.id.drawer_history };
        DialogFactory.buildSingleChoiceDialog(getActivity(), "首页选择", R.array.index_items, -1,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHomeChoice = array[which];
                    }
                },
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CimocApplication.getPreferences().putInt(PreferenceMaster.PREF_HOME, mHomeChoice);
                        mIndexSummary.setText(PreferenceMaster.getTitleById(mHomeChoice));
                    }
                }).show();
    }

    @OnClick(R.id.settings_backup_save_btn) void onSaveBtnClick() {
        mPresenter.onBackupBtnClick();
    }

    @OnClick(R.id.settings_other_cache_btn) void onCacheBtnClick() {
        mPresenter.onCacheBtnClick();
    }

    @OnClick(R.id.settings_other_history_btn) void onHistoryBtnClick() {
        mPresenter.onHistoryBtnClick();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mProgressDialog.dismiss();
    }

    @Override
    protected void initPresenter() {
        mPresenter = new SettingsPresenter(this);
    }

    @Override
    protected BasePresenter getPresenter() {
        return mPresenter;
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_settings;
    }

    public void showProgressDialog(String msg) {
        mProgressDialog.setMessage(msg);
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        mProgressDialog.hide();
    }

    public void showSnackbar(String msg) {
        if (getView() != null) {
            Snackbar.make(getView(), msg, Snackbar.LENGTH_SHORT).show();
        }
    }



}
