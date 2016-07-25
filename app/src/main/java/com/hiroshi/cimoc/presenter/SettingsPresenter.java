package com.hiroshi.cimoc.presenter;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.hiroshi.cimoc.R;
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

    public void cleanCache() {
        mSettingsFragment.showAlertDialog("正在删除..");
        FileUtils.deleteDir(mSettingsFragment.getActivity().getCacheDir());
        mSettingsFragment.showSnackbar("删除成功");
        mSettingsFragment.hideAlertDialog();
    }

    public void backupComic() {
        mSettingsFragment.showAlertDialog("正在备份..");
        List<Comic> list = mComicManager.listFavorite();
        if (BackupUtils.saveComic(list)) {
            mSettingsFragment.showSnackbar("备份成功 共 " + list.size() + " 条记录");
        } else {
            mSettingsFragment.showSnackbar("备份失败 共 " + list.size() + " 条记录");
        }
        mSettingsFragment.hideAlertDialog();
    }

    private int choice;

    public void restoreComic() {
        final String[] files = BackupUtils.showBackupFiles();
        AlertDialog.Builder builder = new AlertDialog.Builder(mSettingsFragment.getActivity(), R.style.AppTheme_Dialog_Alert);
        builder.setTitle("选择文件");
        builder.setSingleChoiceItems(files, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                choice = which;
            }
        });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSettingsFragment.showAlertDialog("正在恢复..");
                List<Comic> list = BackupUtils.restoreComic(files[choice]);
                mComicManager.restoreFavorite(list);
            }
        });
        builder.show();
    }

    public void cleanHistory() {
        mSettingsFragment.showAlertDialog("正在删除..");
        List<Comic> list = mComicManager.listHistory();
        mComicManager.cleanHistory(list);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventMessage msg) {
        switch (msg.getType()) {
            case EventMessage.DELETE_HISTORY:
                int count = (int) msg.getData();
                mSettingsFragment.hideAlertDialog();
                mSettingsFragment.showSnackbar("删除成功 共 " + count + " 条记录");
                break;
            case EventMessage.RESTORE_FAVORITE:
                List<Comic> list = (List<Comic>) msg.getData();
                mSettingsFragment.hideAlertDialog();
                mSettingsFragment.showSnackbar("恢复成功 共 " + list.size() + " 条记录");
                break;
        }
    }

}
