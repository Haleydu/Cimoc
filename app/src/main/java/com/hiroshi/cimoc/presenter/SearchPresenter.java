package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.ui.view.SearchView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class SearchPresenter extends BasePresenter<SearchView> {

    private SourceManager mSourceManager;

    public SearchPresenter() {
        mSourceManager = SourceManager.getInstance();
    }

    public void load() {
        mCompositeSubscription.add(mSourceManager.listEnable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Source>>() {
                    @Override
                    public void call(List<Source> list) {
                        mBaseView.onSourceLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onSourceLoadFail();
                    }
                }));
    }

}
