package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.ui.view.ResultView;

import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class ResultPresenter extends BasePresenter<ResultView> {

    private static final int SEARCH_NULL = 0;
    private static final int SEARCH_DOING = 1;
    private static final int SEARCH_EMPTY = 2;
    private static final int SEARCH_ERROR = 3;

    private List<Integer> list;
    private int[] page;
    private int[] state;
    private String keyword;

    public ResultPresenter(List<Integer> list, String keyword) {
        this.keyword = keyword;
        this.list = list;
        this.page = new int[list.size()];
        this.state = new int[list.size()];
    }

    public void load() {
        for (int i = 0; i != list.size(); ++i) {
            final int pos = i;
            if (state[pos] == SEARCH_NULL) {
                state[pos] = SEARCH_DOING;
                Manga.search(list.get(i), keyword, ++page[pos])
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
                                state[pos] = SEARCH_NULL;
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (page[pos] == 1) {
                                    if (e instanceof Manga.EmptyResultException) {
                                        state[pos] = SEARCH_EMPTY;
                                        for (int i : state) {
                                            if (i != SEARCH_EMPTY) {
                                                return;
                                            }
                                        }
                                        mBaseView.onResultEmpty();
                                    } else {
                                        state[pos] = SEARCH_ERROR;
                                        for (int i : state) {
                                            if (i != SEARCH_ERROR) {
                                                return;
                                            }
                                        }
                                        mBaseView.onSearchError();
                                    }
                                }
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
