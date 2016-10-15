package com.hiroshi.cimoc.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CheckBox;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.presenter.SettingsPresenter;
import com.hiroshi.cimoc.ui.activity.settings.ReaderConfigActivity;
import com.hiroshi.cimoc.ui.view.SettingsView;
import com.hiroshi.cimoc.utils.DialogUtils;
import com.hiroshi.cimoc.utils.ThemeUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class SettingsActivity extends BackActivity implements SettingsView {

    @BindView(R.id.settings_layout) View mSettingsLayout;
    @BindView(R.id.settings_reader_bright_checkbox) CheckBox mBrightBox;
    @BindView(R.id.settings_reader_hide_checkbox) CheckBox mHideBox;

    private SettingsPresenter mPresenter;

    private int mHomeChoice;
    private int mThemeChoice;
    private int mReaderModeChoice;
    private int mTempChoice;

    private DialogInterface.OnClickListener mSingleChoiceListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mTempChoice = which;
        }
    };

    @Override
    protected void initPresenter() {
        mPresenter = new SettingsPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mHomeChoice = mPreference.getInt(PreferenceManager.PREF_LAUNCH_HOME, PreferenceManager.HOME_SEARCH);
        mThemeChoice = mPreference.getInt(PreferenceManager.PREF_THEME, ThemeUtils.THEME_BLUE);
        mReaderModeChoice = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        mBrightBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_KEEP_ON, false));
        mHideBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_HIDE_INFO, false));
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showProgressDialog();
                    mPresenter.backup();
                } else {
                    onBackupFail();
                }
                break;
            case 2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showProgressDialog();
                    mPresenter.loadFiles();
                } else {
                    onFilesLoadFail();
                }
        }
    }

    @OnClick({R.id.settings_reader_bright_btn, R.id.settings_reader_hide_btn})
    void onCheckBoxClick(View view) {
        switch (view.getId()) {
            case R.id.settings_reader_bright_btn:
                checkedAndSave(mBrightBox, PreferenceManager.PREF_READER_KEEP_ON);
                break;
            case R.id.settings_reader_hide_btn:
                checkedAndSave(mHideBox, PreferenceManager.PREF_READER_HIDE_INFO);
                break;
        }
    }

    private void checkedAndSave(CheckBox box, String key) {
        boolean checked = !box.isChecked();
        box.setChecked(checked);
        mPreference.putBoolean(key, checked);
    }

    @OnClick(R.id.settings_reader_config_btn) void onReaderConfigBtnClick() {
        Intent intent = new Intent(this, ReaderConfigActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.settings_backup_restore_btn) void onRestoreBtnClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        } else {
            showProgressDialog();
            mPresenter.loadFiles();
        }
    }

    @OnClick(R.id.settings_other_home_btn) void onHomeBtnClick() {
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_home, R.array.home_items, mHomeChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHomeChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_LAUNCH_HOME, mHomeChoice);
                    }
                }).show();
    }

    @OnClick(R.id.settings_reader_mode_btn) void onReaderModeBtnClick() {
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_reader_mode, R.array.reader_mode_items, mReaderModeChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mReaderModeChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_READER_MODE, mReaderModeChoice);
                    }
                }).show();
    }

    @OnClick(R.id.settings_other_theme_btn) void onThemeBtnClick() {
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_theme, R.array.theme_items, mThemeChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mThemeChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_THEME, mThemeChoice);
                        showSnackbar(R.string.settings_other_theme_reboot);
                    }
                }).show();
    }

    @OnClick(R.id.settings_backup_save_btn) void onSaveBtnClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            showProgressDialog();
            mPresenter.backup();
        }
    }

    @OnClick(R.id.settings_other_cache_btn) void onCacheBtnClick() {
        showProgressDialog();
        mPresenter.clearCache();
    }

    @Override
    public void onFilesLoadSuccess(final String[] files) {
        hideProgressDialog();
        mTempChoice = -1;
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_file, files, -1, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mTempChoice != -1) {
                            showProgressDialog();
                            mPresenter.restore(files[mTempChoice]);
                        }
                    }
                }).show();
    }

    @Override
    public void onFilesLoadFail() {
        hideProgressDialog();
        showSnackbar(R.string.settings_backup_save_not_found);
    }

    @Override
    public void onRestoreSuccess(int count) {
        hideProgressDialog();
        showSnackbar(R.string.settings_backup_restore_success, count);
    }

    @Override
    public void onRestoreFail() {
        hideProgressDialog();
        showSnackbar(R.string.settings_backup_restore_fail);
    }

    @Override
    public void onBackupSuccess(int count) {
        hideProgressDialog();
        showSnackbar(R.string.settings_backup_save_success, count);
    }

    @Override
    public void onBackupFail() {
        hideProgressDialog();
        showSnackbar(R.string.settings_backup_save_fail);
    }

    @Override
    public void onCacheClearSuccess() {
        hideProgressDialog();
        showSnackbar(R.string.settings_other_cache_success);
    }

    @Override
    public void onCacheClearFail() {
        hideProgressDialog();
        showSnackbar(R.string.settings_other_cache_fail);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.drawer_settings);
    }

    @Override
    protected View getLayoutView() {
        return mSettingsLayout;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_settings;
    }
    
}
