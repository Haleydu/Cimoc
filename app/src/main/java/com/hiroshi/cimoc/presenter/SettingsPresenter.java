package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.ui.fragment.SettingsFragment;
import com.hiroshi.cimoc.utils.BackupUtils;
import com.hiroshi.cimoc.utils.FileUtils;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsPresenter extends BasePresenter {

    private SettingsFragment mSettingsFragment;
    private ComicManager mComicManager;

    public SettingsPresenter(SettingsFragment fragment) {
        mSettingsFragment = fragment;
        mComicManager = ComicManager.getInstance();
    }

    public void onCacheBtnClick() {
        mSettingsFragment.showProgressDialog("正在删除..");
        FileUtils.deleteDir(mSettingsFragment.getActivity().getCacheDir());
        mSettingsFragment.showSnackbar("删除成功");
        mSettingsFragment.hideProgressDialog();
    }

    public void onBackupBtnClick() {
        mSettingsFragment.showProgressDialog("正在备份..");
        List<Comic> list = mComicManager.listBackup();
        if (BackupUtils.saveComic(list)) {
            mSettingsFragment.showSnackbar("备份成功 共 " + list.size() + " 条记录");
        } else {
            mSettingsFragment.showSnackbar("备份失败 共 " + list.size() + " 条记录");
        }
        mSettingsFragment.hideProgressDialog();
    }

    public String[] getFiles() {
        String[] files = BackupUtils.showBackupFiles();
        if (files == null || files.length == 0) {
            return null;
        }
        return files;
    }

    public void onRestorePositiveBtnClick(String name) {
        mSettingsFragment.showProgressDialog("正在恢复..");
        List<Comic> list = BackupUtils.restoreComic(name);
        mComicManager.restoreFavorite(list);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.RESTORE_FAVORITE:
                List<Comic> list = (List<Comic>) msg.getData();
                mSettingsFragment.hideProgressDialog();
                mSettingsFragment.showSnackbar("恢复成功 共 " + list.size() + " 条记录");
                break;
        }
    }

}
