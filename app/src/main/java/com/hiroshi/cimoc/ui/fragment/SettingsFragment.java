package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.presenter.SettingsPresenter;
import com.hiroshi.cimoc.ui.activity.BaseActivity;
import com.hiroshi.cimoc.utils.DialogFactory;
import com.hiroshi.cimoc.utils.PreferenceMaster;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsFragment extends BaseFragment {

    @BindView(R.id.settings_other_home_summary) TextView mHomeSummary;
    @BindView(R.id.settings_reader_mode_summary) TextView mModeSummary;
    @BindView(R.id.settings_reader_volume_checkbox) CheckBox mVolumeBox;
    @BindView(R.id.settings_other_nightly_checkbox) CheckBox mNightlyBox;

    private SettingsPresenter mPresenter;
    private PreferenceMaster mPreference;
    private AlertDialog mProgressDialog;

    private int mBackupChoice;
    private int mHomeChoice;
    private int mModeChoice;
    private int mTempChoice;
    private boolean mVolumeChoice;
    private boolean mNightlyChoice;

    private OnClickListener mSingleChoiceListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mTempChoice = which;
        }
    };

    @Override
    protected void initView() {
        mPreference = CimocApplication.getPreferences();
        mProgressDialog = DialogFactory.buildCancelableFalseDialog(getActivity());
        mHomeChoice = mPreference.getInt(PreferenceMaster.PREF_HOME, PreferenceMaster.HOME_CIMOC);
        mModeChoice = mPreference.getInt(PreferenceMaster.PREF_MODE, PreferenceMaster.MODE_HORIZONTAL_PAGE);
        mVolumeChoice = mPreference.getBoolean(PreferenceMaster.PREF_VOLUME, false);
        mNightlyChoice = mPreference.getBoolean(PreferenceMaster.PREF_NIGHTLY, false);
        mHomeSummary.setText(getResources().getStringArray(R.array.home_items)[mHomeChoice]);
        mModeSummary.setText(getResources().getStringArray(R.array.mode_items)[mModeChoice]);
        mVolumeBox.setChecked(mVolumeChoice);
        mNightlyBox.setChecked(mNightlyChoice);
    }

    @OnClick(R.id.settings_other_nightly_btn) void onNightlyBtnClick() {
        mNightlyChoice = !mNightlyChoice;
        mNightlyBox.setChecked(mNightlyChoice);
        if (mNightlyChoice) {
            ((BaseActivity) getActivity()).nightlyOn();
        } else {
            ((BaseActivity) getActivity()).nightlyOff();
        }
        mPreference.putBoolean(PreferenceMaster.PREF_NIGHTLY, mNightlyChoice);
    }

    @OnClick(R.id.settings_reader_volume_btn) void onVolumeBtnClick() {
        mVolumeChoice = !mVolumeChoice;
        mVolumeBox.setChecked(mVolumeChoice);
        mPreference.putBoolean(PreferenceMaster.PREF_VOLUME, mVolumeChoice);
    }

    @OnClick(R.id.settings_backup_restore_btn) void onRestoreBtnClick() {
        final String[] array = mPresenter.getFiles();
        if (array == null) {
            showSnackbar(R.string.settings_backup_save_not_found);
            return;
        }
        DialogFactory.buildSingleChoiceDialog(getActivity(), R.string.settings_select_file, array, -1, mSingleChoiceListener,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBackupChoice = mTempChoice;
                        mPresenter.onRestorePositiveBtnClick(array[mBackupChoice]);
                    }
                }).show();
    }

    @OnClick(R.id.settings_other_home_btn) void onHomeBtnClick() {
        final int[] array = new int[] { R.id.drawer_cimoc, R.id.drawer_favorite, R.id.drawer_history };
        DialogFactory.buildSingleChoiceDialog(getActivity(), R.string.settings_select_home, R.array.home_items, mHomeChoice, mSingleChoiceListener,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mHomeChoice = mTempChoice;
                        mPreference.putInt(PreferenceMaster.PREF_HOME, mHomeChoice);
                        mHomeSummary.setText(getResources().getStringArray(R.array.home_items)[mHomeChoice]);
                    }
                }).show();
    }

    @OnClick(R.id.settings_reader_mode_btn) void onModeBtnClick() {
        DialogFactory.buildSingleChoiceDialog(getActivity(), R.string.settings_select_mode, R.array.mode_items, mModeChoice, mSingleChoiceListener,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mModeChoice = mTempChoice;
                        mPreference.putInt(PreferenceMaster.PREF_MODE, mModeChoice);
                        mModeSummary.setText(getResources().getStringArray(R.array.mode_items)[mModeChoice]);
                    }
                }).show();
    }

    @OnClick(R.id.settings_backup_save_btn) void onSaveBtnClick() {
        mPresenter.onBackupBtnClick();
    }

    @OnClick(R.id.settings_other_cache_btn) void onCacheBtnClick() {
        mPresenter.onCacheBtnClick();
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

    public void showProgressDialog(int resId) {
        mProgressDialog.setMessage(getString(resId));
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        mProgressDialog.hide();
    }

}
