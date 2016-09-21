package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.ui.view.CimocView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/8/12.
 */
public class CimocPresenter extends BasePresenter<CimocView> {

    private SourceManager mSourceManager;

    public CimocPresenter() {
        mSourceManager = SourceManager.getInstance();
    }

    public void load() {
        mSourceManager.listEnable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Source>>() {
                    @Override
                    public void call(List<Source> list) {
                        mBaseView.onLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onLoadFail();
                    }
                });
    }

}
