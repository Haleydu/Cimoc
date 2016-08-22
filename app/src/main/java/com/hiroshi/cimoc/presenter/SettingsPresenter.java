package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.SettingsView;
import com.hiroshi.cimoc.utils.BackupUtils;
import com.hiroshi.cimoc.utils.FileUtils;

import java.io.File;
import java.util.List;

import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsPresenter extends BasePresenter<SettingsView> {

    private ComicManager mComicManager;

    public SettingsPresenter() {
        mComicManager = ComicManager.getInstance();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.RESTORE_FAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                List<Comic> list = (List<Comic>) rxEvent.getData();
                mBaseView.onRestoreSuccess(list.size());
            }
        });
    }

    public void deleteDir(File dir) {
        FileUtils.deleteDir(dir);
    }

    public int backup() {
        List<Comic> list = mComicManager.listBackup();
        if (BackupUtils.saveComic(list)) {
            return list.size();
        } else {
            return -1;
        }
    }

    public String[] getFiles() {
        String[] files = BackupUtils.showBackupFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        return files;
    }

    public void restore(String name) {
        List<Comic> list = BackupUtils.restoreComic(name);
        mComicManager.restoreFavorite(list);
    }

}
