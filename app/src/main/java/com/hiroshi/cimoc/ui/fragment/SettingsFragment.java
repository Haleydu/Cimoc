package com.hiroshi.cimoc.ui.fragment;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.presenter.SettingsPresenter;
import com.hiroshi.cimoc.ui.activity.MainActivity;
import com.hiroshi.cimoc.ui.view.SettingsView;
import com.hiroshi.cimoc.utils.DialogFactory;

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
    @BindView(R.id.settings_reader_bright_checkbox) CheckBox mBrightBox;
    @BindView(R.id.settings_other_night_checkbox) CheckBox mNightBox;

    private SettingsPresenter mPresenter;
    private PreferenceManager mPreference;

    private int mBackupChoice;
    private int mHomeChoice;
    private int mModeChoice;
    private int mTempChoice;
    private boolean mSplitChoice;
    private boolean mVolumeChoice;
    private boolean mNightChoice;
    private boolean mReverseChoice;
    private boolean mBrightChoice;

    private OnClickListener mSingleChoiceListener = new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            mTempChoice = which;
        }
    };

    @Override
    protected void initView() {
        mPreference = CimocApplication.getPreferences();
        mHomeChoice = mPreference.getInt(PreferenceManager.PREF_HOME, PreferenceManager.HOME_CIMOC);
        mModeChoice = mPreference.getInt(PreferenceManager.PREF_MODE, PreferenceManager.MODE_HORIZONTAL_PAGE);
        mSplitChoice = mPreference.getBoolean(PreferenceManager.PREF_SPLIT, false);
        mVolumeChoice = mPreference.getBoolean(PreferenceManager.PREF_VOLUME, false);
        mNightChoice = mPreference.getBoolean(PreferenceManager.PREF_NIGHT, false);
        mReverseChoice = mPreference.getBoolean(PreferenceManager.PREF_REVERSE, false);
        mBrightChoice = mPreference.getBoolean(PreferenceManager.PREF_BRIGHT, false);
        mHomeSummary.setText(getResources().getStringArray(R.array.home_items)[mHomeChoice]);
        mModeSummary.setText(getResources().getStringArray(R.array.mode_items)[mModeChoice]);
        mVolumeBox.setChecked(mVolumeChoice);
        mNightBox.setChecked(mNightChoice);
        mReverseBox.setChecked(mReverseChoice);
        mSplitBox.setChecked(mSplitChoice);
        mBrightBox.setChecked(mBrightChoice);
    }

    @OnClick(R.id.settings_other_night_btn) void onNightBtnClick() {
        mNightChoice = !mNightChoice;
        mNightBox.setChecked(mNightChoice);
        mPreference.putBoolean(PreferenceManager.PREF_NIGHT, mNightChoice);
        ((MainActivity) getActivity()).restart();
    }

    @OnClick(R.id.settings_reader_split_btn) void onSplitBtnClick() {
        mSplitChoice = !mSplitChoice;
        mSplitBox.setChecked(mSplitChoice);
        mPreference.putBoolean(PreferenceManager.PREF_SPLIT, mSplitChoice);
    }

    @OnClick(R.id.settings_reader_volume_btn) void onVolumeBtnClick() {
        mVolumeChoice = !mVolumeChoice;
        mVolumeBox.setChecked(mVolumeChoice);
        mPreference.putBoolean(PreferenceManager.PREF_VOLUME, mVolumeChoice);
    }

    @OnClick(R.id.settings_reader_reverse_btn) void onReverseClick() {
        mReverseChoice = !mReverseChoice;
        mReverseBox.setChecked(mReverseChoice);
        mPreference.putBoolean(PreferenceManager.PREF_REVERSE, mReverseChoice);
    }

    @OnClick(R.id.settings_reader_bright_btn) void onBrightClick() {
        mBrightChoice = !mBrightChoice;
        mBrightBox.setChecked(mBrightChoice);
        mPreference.putBoolean(PreferenceManager.PREF_BRIGHT, mBrightChoice);
    }

    @OnClick(R.id.settings_backup_restore_btn) void onRestoreBtnClick() {
        showProgressDialog();
        mPresenter.loadFiles();
    }

    @OnClick(R.id.settings_other_home_btn) void onHomeBtnClick() {
        DialogFactory.buildSingleChoiceDialog(getActivity(), R.string.settings_select_home, R.array.home_items, mHomeChoice, mSingleChoiceListener,
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
        DialogFactory.buildSingleChoiceDialog(getActivity(), R.string.settings_select_mode, R.array.mode_items, mModeChoice, mSingleChoiceListener,
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
        showProgressDialog();
        mPresenter.backup();
    }

    @OnClick(R.id.settings_other_cache_btn) void onCacheBtnClick() {
        showProgressDialog();
        mPresenter.clearCache(getActivity().getCacheDir());
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        super.onDestroy();
    }

    @Override
    public void onCacheClearSuccess() {
        hideProgressDialog();
        showSnackbar(R.string.settings_other_cache_success);
    }

    @Override
    public void onRestoreSuccess(int count) {
        hideProgressDialog();
        showSnackbar(R.string.settings_backup_restore_success, count);
    }

    @Override
    public void onBackupFail() {
        hideProgressDialog();
        showSnackbar(R.string.settings_backup_save_fail);
    }

    @Override
    public void onBackupSuccess(int count) {
        hideProgressDialog();
        showSnackbar(R.string.settings_backup_save_success, count);
    }

    @Override
    public void onFilesLoadFail() {
        hideProgressDialog();
        showSnackbar(R.string.settings_backup_save_not_found);
    }

    @Override
    public void onFilesLoadSuccess(final String[] files) {
        hideProgressDialog();
        DialogFactory.buildSingleChoiceDialog(getActivity(), R.string.settings_select_file, files, -1, mSingleChoiceListener,
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mBackupChoice = mTempChoice;
                        showProgressDialog();
                        mPresenter.restore(files[mBackupChoice]);
                    }
                }).show();
    }

    @Override
    protected void initPresenter() {
        mPresenter = new SettingsPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_settings;
    }

}
