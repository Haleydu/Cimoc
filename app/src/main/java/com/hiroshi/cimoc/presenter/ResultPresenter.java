package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.source.base.Parser;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.view.ResultView;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class ResultPresenter extends BasePresenter<ResultView> {

    private String keyword;
    private int page;
    private boolean isLoading;
    private Parser parser;

    public ResultPresenter(int source, String keyword) {
        this.keyword = keyword;
        this.page = 0;
        this.isLoading = false;
        this.parser = SourceManager.getParser(source);
    }

    public void load() {
        if (!isLoading) {
            isLoading = true;
            Manga.search(parser, keyword, ++page)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Comic>>() {
                        @Override
                        public void onCompleted() {
                            if (page == 1) {
                                mBaseView.showLayout();
                            }
                            isLoading = false;
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (page == 1) {
                                if (e instanceof Manga.NetworkErrorException) {
                                    mBaseView.onNetworkError();
                                } else if (e instanceof Manga.EmptyResultException) {
                                    mBaseView.onEmptyResult();
                                } else {
                                    mBaseView.onParseError();
                                }
                            }
                        }

                        @Override
                        public void onNext(List<Comic> list) {
                            mBaseView.onLoadSuccess(list);
                        }
                    });
        }
    }

}
