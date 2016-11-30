package com.hiroshi.cimoc.ui.activity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.presenter.SettingsPresenter;
import com.hiroshi.cimoc.service.DownloadService;
import com.hiroshi.cimoc.ui.activity.settings.ReaderConfigActivity;
import com.hiroshi.cimoc.ui.fragment.dialog.ChoiceDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.EditorDialogFragment;
import com.hiroshi.cimoc.ui.fragment.dialog.SliderDialogFragment;
import com.hiroshi.cimoc.ui.view.SettingsView;
import com.hiroshi.cimoc.utils.PermissionUtils;
import com.hiroshi.cimoc.utils.ThemeUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public class SettingsActivity extends BackActivity implements SettingsView, EditorDialogFragment.EditorDialogListener,
        SliderDialogFragment.SliderDialogListener, ChoiceDialogFragment.ChoiceDialogListener {

    private static final int TYPE_OTHER_LAUNCH = 0;
    private static final int TYPE_READER_MODE = 1;
    private static final int TYPE_OTHER_THEME = 2;

    @BindViews({R.id.settings_reader_title, R.id.settings_download_title, R.id.settings_other_title})
    List<TextView> mTitleList;
    @BindView(R.id.settings_layout) View mSettingsLayout;
    @BindView(R.id.settings_reader_bright_checkbox) AppCompatCheckBox mBrightBox;
    @BindView(R.id.settings_reader_hide_checkbox) AppCompatCheckBox mHideBox;

    private SettingsPresenter mPresenter;

    private int mLaunchChoice;
    private int mThemeChoice;
    private int mReaderModeChoice;
    private int mConnectionValue;
    private String mStoragePath;
    private String mTempPath;

    @Override
    protected void initPresenter() {
        mPresenter = new SettingsPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void initView() {
        super.initView();
        mLaunchChoice = mPreference.getInt(PreferenceManager.PREF_OTHER_LAUNCH, PreferenceManager.HOME_SEARCH);
        mThemeChoice = mPreference.getInt(PreferenceManager.PREF_OTHER_THEME, ThemeUtils.THEME_BLUE);
        mReaderModeChoice = mPreference.getInt(PreferenceManager.PREF_READER_MODE, PreferenceManager.READER_MODE_PAGE);
        mStoragePath = mPreference.getString(PreferenceManager.PREF_OTHER_STORAGE, Environment.getExternalStorageDirectory().getAbsolutePath());
        mConnectionValue = mPreference.getInt(PreferenceManager.PREF_DOWNLOAD_CONNECTION, 0);
        mBrightBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_KEEP_ON, false));
        mHideBox.setChecked(mPreference.getBoolean(PreferenceManager.PREF_READER_HIDE_INFO, false));
    }

    @Override
    public void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
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

    @OnClick(R.id.settings_reader_mode_btn) void onReaderModeClick() {
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.settings_reader_mode,
                getResources().getStringArray(R.array.reader_mode_items), mReaderModeChoice, TYPE_READER_MODE);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_other_launch_btn) void onOtherLaunchClick() {
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.settings_other_launch,
                getResources().getStringArray(R.array.home_items), mLaunchChoice, TYPE_OTHER_LAUNCH);
        fragment.show(getFragmentManager(), null);
    }

    @OnClick(R.id.settings_other_theme_btn) void onOtherThemeBtnClick() {
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(R.string.settings_other_theme,
                getResources().getStringArray(R.array.theme_items), mThemeChoice, TYPE_OTHER_THEME);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onChoicePositiveClick(int type, int choice, String value) {
        switch (type) {
            case TYPE_OTHER_LAUNCH:
                mPreference.putInt(PreferenceManager.PREF_OTHER_LAUNCH, choice);
                mLaunchChoice = choice;
                break;
            case TYPE_READER_MODE:
                mPreference.putInt(PreferenceManager.PREF_READER_MODE, choice);
                mReaderModeChoice = choice;
                break;
            case TYPE_OTHER_THEME:
                if (mThemeChoice != choice) {
                    mPreference.putInt(PreferenceManager.PREF_OTHER_THEME, choice);
                    mThemeChoice = choice;
                    int theme = ThemeUtils.getThemeById(choice);
                    setTheme(theme);
                    int primary = ThemeUtils.getResourceId(this, R.attr.colorPrimary);
                    int accent = ThemeUtils.getResourceId(this, R.attr.colorAccent);
                    changeTheme(primary, accent);
                    mPresenter.changeTheme(theme, primary, accent);
                }
                break;
        }
    }

    private void changeTheme(int primary, int accent) {
        if (mToolbar != null) {
            mToolbar.setBackgroundColor(ContextCompat.getColor(this, primary));
        }
        for (TextView textView : mTitleList) {
            textView.setTextColor(ContextCompat.getColor(this, primary));
        }
        ColorStateList stateList = new ColorStateList(new int[][]{{ -android.R.attr.state_checked }, { android.R.attr.state_checked }},
                new int[]{0x8A000000, ContextCompat.getColor(this, accent)});
        mBrightBox.setSupportButtonTintList(stateList);
        mHideBox.setSupportButtonTintList(stateList);
    }

    @OnClick(R.id.settings_other_storage_btn) void onOtherStorageClick() {
        EditorDialogFragment fragment = EditorDialogFragment.newInstance(R.string.settings_other_storage, mStoragePath);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onEditorPositiveClick(String text) {
        if (text != null) {
            stopService(new Intent(this, DownloadService.class));
            showProgressDialog();
            mTempPath = text.trim();
            if (PermissionUtils.hasStoragePermission(this)) {
                mPresenter.moveFiles(mTempPath);
            } else {
                onFileMoveFail();
            }
        }
    }

    @OnClick(R.id.settings_download_connection_btn) void onDownloadConnectionClick() {
        SliderDialogFragment fragment =
                SliderDialogFragment.newInstance(R.string.settings_download_connection, 0, 10, mConnectionValue);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onSliderPositiveClick(int value) {
        mPreference.putInt(PreferenceManager.PREF_DOWNLOAD_CONNECTION, value);
        mConnectionValue = value;
    }

    @OnClick(R.id.settings_other_cache_btn) void onOtherCacheClick() {
        showProgressDialog();
        mPresenter.clearCache();
        showSnackbar(R.string.settings_other_cache_success);
        hideProgressDialog();
    }

    @Override
    public void onFileMoveSuccess() {
        mPreference.putString(PreferenceManager.PREF_OTHER_STORAGE, mTempPath);
        mStoragePath = mTempPath;
        showSnackbar(R.string.settings_other_storage_move_success);
        hideProgressDialog();
    }

    @Override
    public void onFileMoveFail() {
        showSnackbar(R.string.settings_other_storage_move_fail);
        hideProgressDialog();
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
