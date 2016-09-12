package com.hiroshi.cimoc.ui.view;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface SettingsView extends BaseView {

    void onFilesLoadSuccess(String[] files);

    void onFilesLoadFail();

    void onRestoreSuccess(int count);

    void onRestoreFail();

    void onBackupSuccess(int count);

    void onBackupFail();

    void onCacheClearSuccess();

    void onCacheClearFail();

}
