package com.hiroshi.cimoc.ui.view;

/**
 * Created by Hiroshi on 2016/10/19.
 */

public interface BackupView extends BaseView {

    void onBackupSaveSuccess(int size);

    void onBackupSaveFail();

    void onBackupRestoreSuccess(int size);

    void onBackupRestoreFail();

    void onFavoriteFileLoadSuccess(String[] file);

    void onTagFileLoadSuccess(String[] file);

    void onTagLoadSuccess(String[] tag);

    void onTagLoadFail();

    void onFileLoadFail();

}
