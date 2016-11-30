package com.hiroshi.cimoc.ui.activity;

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

public class BackupActivity extends BackActivity implements BackupView, ChoiceDialogFragment.ChoiceDialogListener {

    private static final int TYPE_FAVORITE_FILE = 0;
    private static final int TYPE_TAG_FILE = 1;
    private static final int TYPE_TAG = 2;

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
    public void onChoicePositiveClick(int type, int choice, String value) {
        switch (type) {
            case TYPE_FAVORITE_FILE:
                showProgressDialog();
                mPresenter.restoreFavorite(value);
                break;
            case TYPE_TAG_FILE:
                showProgressDialog();
                mPresenter.restoreTag(value);
                break;
            case TYPE_TAG:
                showProgressDialog();
                mPresenter.saveTag(choice);
                break;
        }
    }

    @Override
    public void onTagLoadSuccess(String[] tag) {
        if (tag.length != 0) {
            showChoiceDialog(R.string.backup_save_tag, tag, TYPE_TAG);
        } else {
            showSnackbar(R.string.backup_save_tag_not_found);
            hideProgressDialog();
        }
    }

    @Override
    public void onFavoriteFileLoadSuccess(String[] file) {
        showChoiceDialog(R.string.backup_restore_favorite, file, TYPE_FAVORITE_FILE);
    }

    @Override
    public void onTagFileLoadSuccess(String[] file) {
        showChoiceDialog(R.string.backup_restore_tag, file, TYPE_TAG_FILE);
    }

    private void showChoiceDialog(int title, String[] item, int type) {
        hideProgressDialog();
        ChoiceDialogFragment fragment = ChoiceDialogFragment.newInstance(title, item, -1, type);
        fragment.show(getFragmentManager(), null);
    }

    @Override
    public void onTagLoadFail() {
        showSnackbar(R.string.backup_save_tag_load_fail);
        hideProgressDialog();
    }

    @Override
    public void onFileLoadFail() {
        showSnackbar(R.string.backup_restore_not_found);
        hideProgressDialog();
    }

    @Override
    public void onBackupRestoreSuccess() {
        showSnackbar(R.string.backup_restore_success);
        hideProgressDialog();
    }

    @Override
    public void onBackupRestoreFail() {
        showSnackbar(R.string.backup_restore_fail);
        hideProgressDialog();
    }

    @Override
    public void onBackupSaveSuccess(int size) {
        showSnackbar(StringUtils.format(getString(R.string.backup_save_success), size));
        hideProgressDialog();
    }

    @Override
    public void onBackupSaveFail() {
        showSnackbar(R.string.backup_save_fail);
        hideProgressDialog();
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
