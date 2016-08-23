package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.HistoryView;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observables.GroupedObservable;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/7/18.
 */
public class HistoryPresenter extends BasePresenter<HistoryView> {

    private ComicManager mComicManager;

    public HistoryPresenter() {
        mComicManager = ComicManager.getInstance();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.HISTORY_COMIC, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onItemUpdate((MiniComic) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.COMIC_DELETE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onSourceRemove((int) rxEvent.getData());
            }
        });
    }

    public void loadComic() {
        mComicManager.listHistory()
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<List<Comic>, Observable<Comic>>() {
                    @Override
                    public Observable<Comic> call(List<Comic> list) {
                        return Observable.from(list);
                    }
                })
                .map(new Func1<Comic, MiniComic>() {
                    @Override
                    public MiniComic call(Comic comic) {
                        return new MiniComic(comic);
                    }
                })
                .toList()
                .subscribe(new Action1<List<MiniComic>>() {
                    @Override
                    public void call(List<MiniComic> list) {
                        mBaseView.onLoadSuccess(list);
                    }
                });
    }

    public void deleteHistory(MiniComic comic) {
        mComicManager.deleteHistory(comic.getId());
    }

    public void clearHistory() {
        mComicManager.listHistory()
                .flatMap(new Func1<List<Comic>, Observable<Comic>>() {
                    @Override
                    public Observable<Comic> call(List<Comic> list) {
                        return Observable.from(list);
                    }
                })
                .groupBy(new Func1<Comic, Boolean>() {
                    @Override
                    public Boolean call(Comic comic) {
                        boolean shouldDelete = comic.getFavorite() == null;
                        if (!shouldDelete) {
                            comic.setHistory(null);
                        }
                        return shouldDelete;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<GroupedObservable<Boolean, Comic>>() {
                    @Override
                    public void onCompleted() {
                        mBaseView.onItemClear();
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(GroupedObservable<Boolean, Comic> booleanComicGroupedObservable) {
                        final boolean shouldDelete = booleanComicGroupedObservable.getKey();
                        booleanComicGroupedObservable
                                .observeOn(Schedulers.io())
                                .subscribeOn(Schedulers.io())
                                .toList()
                                .subscribe(new Action1<List<Comic>>() {
                                    @Override
                                    public void call(List<Comic> list) {
                                        if (shouldDelete) {
                                            mComicManager.deleteInTx(list);
                                        } else {
                                            mComicManager.updateInTx(list);
                                        }
                                    }
                                });
                    }
                });
    }

}
