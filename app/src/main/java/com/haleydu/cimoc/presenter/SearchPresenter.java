package com.haleydu.cimoc.presenter;

import com.haleydu.cimoc.core.Manga;
import com.haleydu.cimoc.manager.SourceManager;
import com.haleydu.cimoc.model.Source;
import com.haleydu.cimoc.ui.view.SearchView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class SearchPresenter extends BasePresenter<SearchView> {

    private SourceManager mSourceManager;

    @Override
    protected void onViewAttach() {
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    public void loadSource() {
        mCompositeSubscription.add(mSourceManager.listEnableInRx()
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

    public void loadAutoComplete(String keyword) {
        mCompositeSubscription.add(Manga.loadAutoComplete(keyword)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> list) {
                        mBaseView.onAutoCompleteLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }));
    }

}
