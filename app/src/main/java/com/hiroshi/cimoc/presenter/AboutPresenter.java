package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Update;
import com.hiroshi.cimoc.ui.view.AboutView;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/8/24.
 */
public class AboutPresenter extends BasePresenter<AboutView> {

    public void checkUpdate(final String version) {
        Update.check()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        mBaseView.onCheckError();
                    }

                    @Override
                    public void onNext(String s) {
                        if (version.equals(s)) {
                            mBaseView.onUpdateNone();
                        } else {
                            mBaseView.onUpdateReady();
                        }
                    }
                });
    }


}
