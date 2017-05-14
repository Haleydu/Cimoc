package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.manager.LocalManager;
import com.hiroshi.cimoc.manager.TagManager;
import com.hiroshi.cimoc.manager.TagRefManager;
import com.hiroshi.cimoc.model.Local;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.ui.view.LocalView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2017/5/14.
 */

public class LocalPresenter extends BasePresenter<LocalView> {

    private LocalManager mLocalManager;

    @Override
    protected void onViewAttach() {
        mLocalManager = LocalManager.getInstance(mBaseView);
    }

    public void load() {
        mCompositeSubscription.add(mLocalManager.listInRx()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Local>>() {
                    @Override
                    public void call(List<Local> list) {
                        mBaseView.onDataLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onDataLoadFail();
                    }
                }));
    }

    public void insert(Local local) {
        mLocalManager.insert(local);
    }

    public void delete(final Local local) {
        mLocalManager.delete(local);
        mBaseView.onDataDeleteSuccess();
    }


}
