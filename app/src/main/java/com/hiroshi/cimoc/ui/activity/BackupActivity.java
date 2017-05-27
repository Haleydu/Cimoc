package com.hiroshi.cimoc.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.presenter.BackupPresenter;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.ui.widget.preference.CheckBoxPreference;
import com.hiroshi.cimoc.ui.fragment.dialog.ChoiceDialogFragment;
import com.hiroshi.cimoc.ui.view.BackupView;
import com.hiroshi.cimoc.utils.PermissionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/10/19.
 */

public class BackupActivity extends BackActivity implements BackupView {

    private static final int DIALOG_REQUEST_RESTORE_FAVORITE = 0;
    private static final int DIALOG_REQUEST_RESTORE_TAG = 1;
    private static final int DIALOG_REQUEST_SAVE_TAG = 2;

    @BindView(R.id.backup_layout) View mLayoutView;
    @BindView(R.id.backup_save_favorite_auto) CheckBoxPreference mSaveFavoriteAuto;

    private BackupPresenter mPresenter;

    private long[] mSavedId;

    @Override
    protected BasePresenter initPresenter() {
        mPresenter = new BackupPresenter();
        mPresenter.attachView(this);
        return mPresenter;
    }

    @Override
    protected void initView() {
        super.initView();
        mSaveFavoriteAuto.bindPreference(PreferenceManager.PREF_BACKUP_SAVE_FAVORITE, true);
    }

    @OnClick(R.id.backup_save_favorite) void onSaveFavoriteClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.saveFavorite();
        } else {
            onFileLoadFail();
        }
    }

    @OnClick(R.id.backup_save_tag) void onSaveTagClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.loadTag();
        } else {
            onFileLoadFail();
        }
    }

    @OnClick(R.id.backup_restore_favorite) void onRestoreFavoriteClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.loadFavoriteFile();
        } else {
            onFileLoadFail();
        }
    }

    @OnClick(R.id.backup_restore_tag) void onRestoreTagClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.loadTagFile();
        } else {
            onFileLoadFail();
        }
    }

    @Override
    public void onDialogResult(int requestCode, Bundle bundle) {
        int choice;
        String value;
        switch (requestCode) {
            case DIALOG_REQUEST_RESTORE_FAVORITE:
                showProgressDialog();
                value = bundle.getString(EXTRA_DIALOG_RESULT_VALUE);
                mPresenter.restoreFavorite(value);
                break;
            case DIALOG_REQUEST_RESTORE_TAG:
                showProgressDialog();
                value = bundle.getString(EXTRA_DIALOG_RESULT_VALUE);
                mPresenter.restoreTag(value);
                break;
            case DIALOG_REQUEST_SAVE_TAG:
                showProgressDialog();
                if (mSavedId != null) {
                    value = bundle.getString(EXTRA_DIALOG_RESULT_VALUE);
                    choice = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                    mPresenter.saveTag(mSavedId[choice], value);
                } else {
                    onBackupSaveFail();
                }
                break;
        }
    }

    @Override
    public void onTagLoadSuccess(List<Tag> list) {
        if (!list.isEmpty()) {
            int size = list.size();
            String[] title = new String[size];
            mSavedId = new long[size];
            for (int i = 0; i != size; ++i) {
                Tag tag = list.get(i);
                mSavedId[i] = tag.getId();
                title[i] = tag.getTitle();
            }
            showChoiceDialog(R.string.backup_save_tag, title, DIALOG_REQUEST_SAVE_TAG);
        } else {
            showSnackbar(R.string.backup_save_tag_not_found);
            hideProgressDialog();
        }
    }

    @Override
    public void onFavoriteFileLoadSuccess(String[] file) {
        showChoiceDialog(R.string.backup_restore_favorite, file, DIALOG_REQUEST_RESTORE_FAVORITE);
    }

    @Override
    public void onTagFileLoadSuccess(String[] file) {
        showChoiceDialog(R.string.backup_restore_tag, file, DIALOG_REQUEST_RESTORE_TAG);
    }

    private void showChoiceDialog(int title, String[] item, int request) {
        hideProgressDialog();
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(title, item, -1, request);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onTagLoadFail() {
        hideProgressDialog();
        showSnackbar(R.string.backup_save_tag_load_fail);
    }

    @Override
    public void onFileLoadFail() {
        hideProgressDialog();
        showSnackbar(R.string.backup_restore_not_found);
    }

    @Override
    public void onBackupRestoreSuccess() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_success);
    }

    @Override
    public void onBackupRestoreFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    public void onBackupSaveSuccess(int size) {
        hideProgressDialog();
        showSnackbar(StringUtils.format(getString(R.string.backup_save_success), size));
    }

    @Override
    public void onBackupSaveFail() {
        hideProgressDialog();
        showSnackbar(R.string.common_execute_fail);
    }

    @Override
    protected String getDefaultTitle() {
        return getString(R.string.drawer_backup);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_backup;
    }

    @Override
    protected View getLayoutView() {
        return mLayoutView;
    }

}
