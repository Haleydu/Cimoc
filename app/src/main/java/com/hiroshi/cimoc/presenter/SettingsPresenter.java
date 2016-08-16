package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.core.manager.ComicManager;
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

    public void clearCache() {
        FileUtils.deleteDir(mSettingsFragment.getActivity().getCacheDir());
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

    @SuppressWarnings("unchecked")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.RESTORE_FAVORITE:
                List<Comic> list = (List<Comic>) msg.getData();
                String text = mSettingsFragment.getString(R.string.settings_backup_restore_success) + list.size();
                mSettingsFragment.showSnackbar(text);
                mSettingsFragment.hideProgressDialog();
                break;
        }
    }

}
