package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.view.ResultView;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class ResultPresenter extends BasePresenter<ResultView> {

    private int[] source;
    private int[] page;
    private boolean[] loading;
    private String keyword;

    public ResultPresenter(int[] source, String keyword) {
        this.keyword = keyword;
        this.source = source;
        this.page = new int[source.length];
        this.loading = new boolean[source.length];
    }

    public void load() {
        for (int i = 0; i != source.length; ++i) {
            final int pos = i;
            if (!loading[pos]) {
                loading[pos] = true;
                Manga.search(source[pos], keyword, ++page[pos])
                        .filter(new Func1<Comic, Boolean>() {
                            @Override
                            public Boolean call(Comic comic) {
                                return comic != null;
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Comic>() {
                            @Override
                            public void onCompleted() {
                                loading[pos] = false;
                            }

                            @Override
                            public void onError(Throwable e) {
                                /*if (page[pos] == 1) {
                                    if (e instanceof Manga.NetworkErrorException) {
                                        mBaseView.onNetworkError();
                                    } else if (e instanceof Manga.EmptyResultException) {
                                        mBaseView.onEmptyResult();
                                    } else {
                                        mBaseView.onParseError();
                                    }
                                }   */
                                e.printStackTrace();
                            }

                            @Override
                            public void onNext(Comic comic) {
                                mBaseView.onLoadSuccess(comic);
                            }
                        });
            }
        }
    }

}
