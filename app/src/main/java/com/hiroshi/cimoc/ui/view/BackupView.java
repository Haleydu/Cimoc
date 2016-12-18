package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/19.
 */

public interface BackupView extends BaseView, DialogView {

    void onBackupSaveSuccess(int size);

    void onBackupSaveFail();

    void onBackupRestoreSuccess();

    void onBackupRestoreFail();

    void onFavoriteFileLoadSuccess(String[] file);

    void onTagFileLoadSuccess(String[] file);

    void onTagLoadSuccess(List<Tag> list);

    void onTagLoadFail();

    void onFileLoadFail();

}
