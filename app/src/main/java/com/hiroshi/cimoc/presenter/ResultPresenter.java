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

    private int emptyNum = 0;
    private int errorNum = 0;

    public ResultPresenter(List<Integer> list, String keyword) {
        this.keyword = keyword;
        this.list = list;
        this.page = new int[list.size()];
        this.state = new int[list.size()];
    }

    private void recent() {
        if (state[0] == SEARCH_NULL) {
            state[0] = SEARCH_DOING;
            Manga.recent(list.get(0), ++page[0])
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Comic>>() {
                        @Override
                        public void onCompleted() {
                            state[0] = SEARCH_NULL;
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (page[0] == 1 && mBaseView != null) {
                                mBaseView.onRecentLoadFail();
                            }
                        }

                        @Override
                        public void onNext(List<Comic> list) {
                            mBaseView.onRecentLoadSuccess(list);
                        }
                    });
        }
    }

    private void search() {
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
                                        if (++emptyNum == state.length && mBaseView != null) {
                                            mBaseView.onResultEmpty();
                                        }
                                    } else if (e instanceof Manga.NetworkErrorException || e instanceof Manga.ParseErrorException) {
                                        state[pos] = SEARCH_ERROR;
                                        if (++errorNum == state.length && mBaseView != null) {
                                            mBaseView.onSearchError();
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onNext(Comic comic) {
                                mBaseView.onSearchSuccess(comic);
                            }
                        });
            }
        }
    }

    public void load() {
        if (keyword == null) {
            recent();
        } else {
            search();
        }
    }

}
