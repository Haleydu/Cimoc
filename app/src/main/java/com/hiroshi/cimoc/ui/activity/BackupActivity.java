package com.hiroshi.cimoc.ui.activity;

import android.os.Bundle;
import android.view.View;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BackupPresenter;
import com.hiroshi.cimoc.ui.fragment.dialog.ChoiceDialogFragment;
import com.hiroshi.cimoc.ui.view.BackupView;
import com.hiroshi.cimoc.utils.PermissionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

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

    private BackupPresenter mPresenter;

    @Override
    protected void initPresenter() {
        mPresenter = new BackupPresenter();
        mPresenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        mPresenter.detachView();
        mPresenter = null;
        super.onDestroy();
    }

    @OnClick(R.id.backup_save_favorite_btn) void onSaveFavoriteClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.saveFavorite();
        } else {
            onFileLoadFail();
        }
    }

    @OnClick(R.id.backup_save_tag_btn) void onSaveTagClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.loadTag();
        } else {
            onFileLoadFail();
        }
    }

    @OnClick(R.id.backup_restore_favorite_btn) void onRestoreFavoriteClick() {
        showProgressDialog();
        if (PermissionUtils.hasStoragePermission(this)) {
            mPresenter.loadFavoriteFile();
        } else {
            onFileLoadFail();
        }
    }

    @OnClick(R.id.backup_restore_tag_btn) void onRestoreTagClick() {
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
                choice = bundle.getInt(EXTRA_DIALOG_RESULT_INDEX);
                mPresenter.saveTag(choice);
                break;
        }
    }

    @Override
    public void onTagLoadSuccess(String[] tag) {
        if (tag.length != 0) {
            showChoiceDialog(R.string.backup_save_tag, tag, DIALOG_REQUEST_SAVE_TAG);
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

    private void showChoiceDialog(int title, String[] item, int type) {
        hideProgressDialog();
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(title, item, -1, type);
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
        showSnackbar(R.string.backup_restore_success);
    }

    @Override
    public void onBackupRestoreFail() {
        hideProgressDialog();
        showSnackbar(R.string.backup_restore_fail);
    }

    @Override
    public void onBackupSaveSuccess(int size) {
        hideProgressDialog();
        showSnackbar(StringUtils.format(getString(R.string.backup_save_success), size));
    }

    @Override
    public void onBackupSaveFail() {
        hideProgressDialog();
        showSnackbar(R.string.backup_save_fail);
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
