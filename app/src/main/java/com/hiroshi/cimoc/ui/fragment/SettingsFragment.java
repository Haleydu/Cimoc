package com.hiroshi.cimoc.ui.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.presenter.SettingsPresenter;
import com.hiroshi.cimoc.ui.activity.MainActivity;
import com.hiroshi.cimoc.ui.view.SettingsView;
import com.hiroshi.cimoc.utils.DialogUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsFragment extends BaseFragment implements SettingsView {

    @BindView(R.id.settings_other_home_summary) TextView mHomeSummary;
    @BindView(R.id.settings_reader_mode_summary) TextView mModeSummary;
    @BindView(R.id.settings_reader_split_checkbox) CheckBox mSplitBox;
    @BindView(R.id.settings_reader_volume_checkbox) CheckBox mVolumeBox;
    @BindView(R.id.settings_reader_reverse_checkbox) CheckBox mReverseBox;
    @BindView(R.id.settings_reader_picture_checkbox) CheckBox mPictureBox;
    @BindView(R.id.settings_reader_bright_checkbox) CheckBox mBrightBox;
    @BindView(R.id.settings_other_night_checkbox) CheckBox mNightBox;

    private SettingsPresenter mPresenter;
    private PreferenceManager mPreference;

    private int mHomeChoice;
    private int mModeChoice;
    private int mTempChoice;

    private OnClickListener mSingleChoiceListener = new OnClickListener() {
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
        mPreference = CimocApplication.getPreferences();
        mHomeChoice = mPreference.getInt(PreferenceManager.PREF_HOME, PreferenceManager.HOME_CIMOC);
        mModeChoice = mPreference.getInt(PreferenceManager.PREF_MODE, PreferenceManager.MODE_HORIZONTAL_PAGE);
        mHomeSummary.setText(getResources().getStringArray(R.array.home_items)[mHomeChoice]);
        mModeSummary.setText(getResources().getStringArray(R.array.mode_items)[mModeChoice]);
        mVolumeBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_VOLUME, false));
        mNightBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_NIGHT, false));
        mReverseBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_REVERSE, false));
        mSplitBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_SPLIT, false));
        mPictureBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_PICTURE, false));
        mBrightBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_BRIGHT, false));
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
        }
    }

    @OnClick({ R.id.settings_other_night_btn, R.id.settings_reader_split_btn, R.id.settings_reader_volume_btn,
            R.id.settings_reader_reverse_btn, R.id.settings_reader_picture_btn, R.id.settings_reader_bright_btn})
    void onCheckBoxClick(View view) {
        switch (view.getId()) {
            case R.id.settings_other_night_btn:
                checkedAndSave(mNightBox, PreferenceManager.PREF_NIGHT);
                ((MainActivity) getActivity()).restart();
                break;
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
        }
    }

    private void checkedAndSave(CheckBox box, String key) {
        boolean checked = !box.isChecked();
        box.setChecked(checked);
        mPreference.putBoolean(key, checked);
    }

    @OnClick(R.id.settings_backup_restore_btn) void onRestoreBtnClick() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            showProgressDialog();
            mPresenter.loadFiles();
        }
    }

    @OnClick(R.id.settings_other_home_btn) void onHomeBtnClick() {
        DialogUtils.buildSingleChoiceDialog(getActivity(), R.string.settings_select_home, R.array.home_items, mHomeChoice, mSingleChoiceListener,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHomeChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_HOME, mHomeChoice);
                        mHomeSummary.setText(getResources().getStringArray(R.array.home_items)[mHomeChoice]);
                    }
                }).show();
    }

    @OnClick(R.id.settings_reader_mode_btn) void onModeBtnClick() {
        DialogUtils.buildSingleChoiceDialog(getActivity(), R.string.settings_select_mode, R.array.mode_items, mModeChoice, mSingleChoiceListener,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mModeChoice = mTempChoice;
                        mPreference.putInt(PreferenceManager.PREF_MODE, mModeChoice);
                        mModeSummary.setText(getResources().getStringArray(R.array.mode_items)[mModeChoice]);
                    }
                }).show();
    }

    @OnClick(R.id.settings_backup_save_btn) void onSaveBtnClick() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            showProgressDialog();
            mPresenter.backup();
        }
    }

    @OnClick(R.id.settings_other_cache_btn) void onCacheBtnClick() {
        showProgressDialog();
        mPresenter.clearCache(getActivity().getCacheDir());
    }

    @Override
    public void onFilesLoadSuccess(final String[] files) {
        hideProgressDialog();
        DialogUtils.buildSingleChoiceDialog(getActivity(), R.string.settings_select_file, files, -1, mSingleChoiceListener,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showProgressDialog();
                        mPresenter.restore(files[mTempChoice]);
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
    protected int getLayoutView() {
        return R.layout.fragment_settings;
    }

}
