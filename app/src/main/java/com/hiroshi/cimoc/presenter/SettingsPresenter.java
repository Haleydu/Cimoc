package com.hiroshi.cimoc.presenter;

import android.support.annotation.ColorRes;
import android.support.annotation.StyleRes;
import android.support.v4.provider.DocumentFile;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.core.Storage;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.SettingsView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsPresenter extends BasePresenter<SettingsView> {

    public void clearCache() {
        Fresco.getImagePipeline().clearDiskCaches();
    }

    public void moveFiles(DocumentFile dst) {
        mCompositeSubscription.add(Storage.moveRootDir(dst)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer value) {
                        switch (value) {
                            //Todo 使用 string.xml
                            case 1:
                                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, "正在移动备份文件"));
                                break;
                            case 2:
                                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, "正在移动下载文件"));
                                break;
                            case 3:
                                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, "正在移动截图文件"));
                                break;
                            case 4:
                                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, "正在删除原文件"));
                                break;
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onFileMoveFail();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mBaseView.onFileMoveSuccess();
                    }
                }));
    }

    public void changeTheme(@StyleRes int theme, @ColorRes int primary, @ColorRes int accent) {
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_THEME_CHANGE, theme, primary, accent));
    }

}
