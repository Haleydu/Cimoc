package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.component.DialogCaller;

/**
 * Created by Hiroshi on 2016/10/19.
 */

public interface BackupView extends BaseView, DialogCaller {

    void onBackupSaveSuccess(int size);

    void onBackupSaveFail();

    void onBackupRestoreSuccess();

    void onBackupRestoreFail();

    void onComicFileLoadSuccess(String[] file);

    void onTagFileLoadSuccess(String[] file);

    void onFileLoadFail();

}
