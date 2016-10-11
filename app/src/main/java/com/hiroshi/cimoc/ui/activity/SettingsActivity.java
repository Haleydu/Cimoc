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

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.presenter.SettingsPresenter;
import com.hiroshi.cimoc.ui.view.SettingsView;
import com.hiroshi.cimoc.utils.DialogUtils;
import com.hiroshi.cimoc.utils.StringUtils;
import com.hiroshi.cimoc.utils.ThemeUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class SettingsActivity extends BackActivity implements SettingsView {

    @BindView(R.id.settings_layout) View mSettingsLayout;
    @BindView(R.id.settings_reader_split_checkbox) CheckBox mSplitBox;
    @BindView(R.id.settings_reader_bright_checkbox) CheckBox mBrightBox;
    @BindView(R.id.settings_reader_hide_checkbox) CheckBox mHideBox;
    @BindView(R.id.settings_reader_blank_checkbox) CheckBox mBlankBox;

    private SettingsPresenter mPresenter;
    private PreferenceManager mPreference;

    private int mHomeChoice;
    private int mThemeChoice;
    private int mReaderModeChoice;
    private int mReaderTurnChoice;
    private int mReaderOrientationChoice;
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
        mHomeChoice = mPreference.getInt(PreferenceManager.PREF_LAUNH_HOME, PreferenceManager.HOME_SEARCH);
        mThemeChoice = mPreference.getInt(PreferenceManager.PREF_THEME, ThemeUtils.THEME_BLUE);
        mReaderModeChoice = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        mReaderTurnChoice = mPreference.getInt(PreferenceManager.PREF_READER_TURN, PreferenceManager.READER_TURN_LTR);
        mReaderOrientationChoice = mPreference.getInt(PreferenceManager.PREF_READER_ORIENTATION, PreferenceManager.READER_ORIENTATION_PORTRAIT);
        mTriggerNum = mPreference.getInt(PreferenceManager.PREF_TRIGGER, 5);
        mSplitBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_SPLIT, false));
        mBrightBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_BRIGHT, false));
        mHideBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_HIDE, false));
        mBlankBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_BLANK, false));
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
                    mProgressDialog.show();
                    mPresenter.backup();
                } else {
                    onBackupFail();
                }
                break;
            case 2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mProgressDialog.show();
                    mPresenter.loadFiles();
                } else {
                    onFilesLoadFail();
                }
        }
    }

    @OnClick({ R.id.settings_reader_split_btn, R.id.settings_reader_bright_btn,
            R.id.settings_reader_hide_btn, R.id.settings_reader_blank_btn})
    void onCheckBoxClick(View view) {
        switch (view.getId()) {
            case R.id.settings_reader_split_btn:
                checkedAndSave(mSplitBox, PreferenceManager.PREF_SPLIT);
                break;
            case R.id.settings_reader_bright_btn:
                checkedAndSave(mBrightBox, PreferenceManager.PREF_BRIGHT);
                break;
            case R.id.settings_reader_hide_btn:
                checkedAndSave(mHideBox, PreferenceManager.PREF_HIDE);
                break;
            case R.id.settings_reader_blank_btn:
                checkedAndSave(mBlankBox, PreferenceManager.PREF_BLANK);
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
                    {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        } else {
            mProgressDialog.show();
            mPresenter.loadFiles();
        }
    }

    @OnClick({ R.id.settings_reader_click_event_btn, R.id.settings_reader_long_click_event_btn })
    void onReaderEventBtnClick(View view) {
        boolean isLong = view.getId() == R.id.settings_reader_long_click_event_btn;
        Intent intent = EventActivity.createIntent(this, isLong, mReaderOrientationChoice == PreferenceManager.READER_ORIENTATION_PORTRAIT);
        startActivity(intent);
    }

    @OnClick(R.id.settings_other_home_btn) void onHomeBtnClick() {
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_home, R.array.home_items, mHomeChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHomeChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_LAUNH_HOME, mHomeChoice);
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

    @OnClick(R.id.settings_reader_turn_btn) void onReaderTurnBtnClick() {
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_reader_turn, R.array.reader_turn_items, mReaderTurnChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mReaderTurnChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_READER_TURN, mReaderTurnChoice);
                    }
                }).show();
    }

    @OnClick(R.id.settings_reader_orientation_btn) void onReaderOrientationBtnClick() {
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_reader_orientation, R.array.reader_orientation_items, mReaderOrientationChoice, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mReaderOrientationChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_READER_ORIENTATION, mReaderOrientationChoice);
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
            mProgressDialog.show();
            mPresenter.backup();
        }
    }

    @OnClick(R.id.settings_other_cache_btn) void onCacheBtnClick() {
        mProgressDialog.show();
        mPresenter.clearCache();
    }

    @Override
    public void onFilesLoadSuccess(final String[] files) {
        mProgressDialog.hide();
        mTempChoice = -1;
        DialogUtils.buildSingleChoiceDialog(this, R.string.settings_select_file, files, -1, mSingleChoiceListener,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mTempChoice != -1) {
                            mProgressDialog.show();
                            mPresenter.restore(files[mTempChoice]);
                        }
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
