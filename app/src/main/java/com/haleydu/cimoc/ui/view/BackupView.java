package com.haleydu.cimoc.ui.view;

import com.haleydu.cimoc.component.DialogCaller;

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

    void onSettingsFileLoadSuccess(String[] file);

    void onClearFileLoadSuccess(String[] file);

    void onFileLoadFail();

    void onClearBackupSuccess();

    void onClearBackupFail();

}
