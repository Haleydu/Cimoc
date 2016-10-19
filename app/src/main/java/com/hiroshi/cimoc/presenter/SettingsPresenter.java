package com.hiroshi.cimoc.presenter;

import android.support.annotation.ColorRes;
import android.support.annotation.StyleRes;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.core.Storage;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.SettingsView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsPresenter extends BasePresenter<SettingsView> {

    public void clearCache() {
        Fresco.getImagePipeline().clearDiskCaches();
    }

    public void moveFiles(final String path) {
        mCompositeSubscription.add(Storage.moveFolder(path)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mBaseView.onFileMoveSuccess(path);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onFileMoveFail();
                    }
                }));
    }

    public void changeTheme(@StyleRes int theme, @ColorRes int primary, @ColorRes int accent) {
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_THEME_CHANGE, theme, primary, accent));
    }

}
