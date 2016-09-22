package com.hiroshi.cimoc.ui.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.presenter.SettingsPresenter;
import com.hiroshi.cimoc.ui.view.SettingsView;
import com.hiroshi.cimoc.utils.DialogUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class SettingsActivity extends BackActivity implements SettingsView {

    @BindView(R.id.settings_layout) View mSettingsLayout;
    @BindView(R.id.settings_other_home_summary) TextView mHomeSummary;
    @BindView(R.id.settings_reader_mode_summary) TextView mModeSummary;
    @BindView(R.id.settings_reader_split_checkbox) CheckBox mSplitBox;
    @BindView(R.id.settings_reader_volume_checkbox) CheckBox mVolumeBox;
    @BindView(R.id.settings_reader_reverse_checkbox) CheckBox mReverseBox;
    @BindView(R.id.settings_reader_picture_checkbox) CheckBox mPictureBox;
    @BindView(R.id.settings_reader_bright_checkbox) CheckBox mBrightBox;
    @BindView(R.id.settings_reader_hide_checkbox) CheckBox mHideBox;

    private SettingsPresenter mPresenter;
    private PreferenceManager mPreference;

    private int mHomeChoice;
    private int mModeChoice;
    private int mTempChoice;
    private int mTriggerNum;

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
    protected void initProgressBar() {}

    @Override
    protected void initView() {
        super.initView();
        mPreference = CimocApplication.getPreferences();
        mHomeChoice = mPreference.getInt(PreferenceManager.PREF_HOME, PreferenceManager.HOME_CIMOC);
        mModeChoice = mPreference.getInt(PreferenceManager.PREF_MODE, PreferenceManager.MODE_HORIZONTAL_PAGE);
        mTriggerNum = mPreference.getInt(PreferenceManager.PREF_TRIGGER, 5);
        mHomeSummary.setText(getResources().getStringArray(R.array.home_items)[mHomeChoice]);
        mModeSummary.setText(getResources().getStringArray(R.array.mode_items)[mModeChoice]);
        mVolumeBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_VOLUME, false));
        mReverseBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_REVERSE, false));
        mSplitBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_SPLIT, false));
        mPictureBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_PICTURE, false));
        mBrightBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_BRIGHT, false));
        mHideBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_HIDE, false));
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mProgressDialog.show();
                    mPresenter.backup();
                } else {
                    onBackupFail();
                }
                break;
        }
    }

    @OnClick({ R.id.settings_reader_split_btn, R.id.settings_reader_volume_btn, R.id.settings_reader_reverse_btn,
            R.id.settings_reader_picture_btn, R.id.settings_reader_bright_btn, R.id.settings_reader_hide_btn})
    void onCheckBoxClick(View view) {
        switch (view.getId()) {
            case R.id.settings_reader_split_btn:
                checkedAndSave(mSplitBox, PreferenceManager.PREF_SPLIT);
                break;
            case R.id.settings_reader_volume_btn:
                checkedAndSave(mVolumeBox, PreferenceManager.PREF_VOLUME);
                break;
            case R.id.settings_reader_reverse_btn:
                checkedAndSave(mReverseBox, PreferenceManager.PREF_REVERSE);
                break;
            case R.id.settings_reader_picture_btn:
                checkedAndSave(mPictureBox, PreferenceManager.PREF_PICTURE);
                break;
            case R.id.settings_reader_bright_btn:
                checkedAndSave(mBrightBox, PreferenceManager.PREF_BRIGHT);
                break;
            case R.id.settings_reader_hide_btn:
                checkedAndSave(mHideBox, PreferenceManager.PREF_HIDE);
                break;
        }
    }

    private void checkedAndSave(CheckBox box, String key) {
        boolean checked = !box.isChecked();
        box.setChecked(checked);
        mPreference.putBoolean(key, checked);
    }

    @OnClick(R.id.settings_reader_trigger_btn) void onTriggerBtnClick() {
        final String[] array = StringUtils.range(5, 50, 5);
        for (int i = 0; i != array.length; ++i) {
            if (Integer.parseInt(array[i]) == mTriggerNum) {
                DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_trigger, array, i,
                        mSingleChoiceListener, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mTriggerNum = Integer.parseInt(array[mTempChoice]);
                                mPreference.putInt(PreferenceManager.PREF_TRIGGER, mTriggerNum);
                            }
                        }).show();
            }
        }
    }

    @OnClick(R.id.settings_backup_restore_btn) void onRestoreBtnClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            mProgressDialog.show();
            mPresenter.loadFiles();
        }
    }

    @OnClick(R.id.settings_other_home_btn) void onHomeBtnClick() {
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_home, R.array.home_items, mHomeChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHomeChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_HOME, mHomeChoice);
                        mHomeSummary.setText(getResources().getStringArray(R.array.home_items)[mHomeChoice]);
                    }
                }).show();
    }

    @OnClick(R.id.settings_reader_mode_btn) void onModeBtnClick() {
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_mode, R.array.mode_items, mModeChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mModeChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_MODE, mModeChoice);
                        mModeSummary.setText(getResources().getStringArray(R.array.mode_items)[mModeChoice]);
                    }
                }).show();
    }

    @OnClick(R.id.settings_backup_save_btn) void onSaveBtnClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            mProgressDialog.show();
            mPresenter.backup();
        }
    }

    @OnClick(R.id.settings_other_cache_btn) void onCacheBtnClick() {
        mProgressDialog.show();
        mPresenter.clearCache(getCacheDir());
    }

    @Override
    public void onFilesLoadSuccess(final String[] files) {
        mProgressDialog.hide();
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_file, files, -1, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mProgressDialog.show();
                        mPresenter.restore(files[mTempChoice]);
                    }
                }).show();
    }

    @Override
    public void onFilesLoadFail() {
        mProgressDialog.hide();
        showSnackbar(R.string.settings_backup_save_not_found);
    }

    @Override
    public void onRestoreSuccess(int count) {
        mProgressDialog.hide();
        showSnackbar(R.string.settings_backup_restore_success, count);
    }

    @Override
    public void onRestoreFail() {
        mProgressDialog.hide();
        showSnackbar(R.string.settings_backup_restore_fail);
    }

    @Override
    public void onBackupSuccess(int count) {
        mProgressDialog.hide();
        showSnackbar(R.string.settings_backup_save_success, count);
    }

    @Override
    public void onBackupFail() {
        mProgressDialog.hide();
        showSnackbar(R.string.settings_backup_save_fail);
    }

    @Override
    public void onCacheClearSuccess() {
        mProgressDialog.hide();
        showSnackbar(R.string.settings_other_cache_success);
    }

    @Override
    public void onCacheClearFail() {
        mProgressDialog.hide();
        showSnackbar(R.string.settings_other_cache_fail);
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
