package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.HistoryView;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/7/18.
 */
public class HistoryPresenter extends GridPresenter<HistoryView> {

    private ComicManager mComicManager;

    public HistoryPresenter() {
        mComicManager = ComicManager.getInstance();
    }

    @Override
    protected void initSubscription() {
        super.initSubscription();
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

    @Override
    protected Observable<List<Comic>> getRawObservable() {
        return mComicManager.listHistory();
    }

    public void deleteHistory(MiniComic history) {
        Comic comic = mComicManager.load(history.getId());
        if (comic.getFavorite() == null && comic.getDownload() == null) {
            mComicManager.delete(comic);
        } else {
            comic.setHistory(null);
            mComicManager.update(comic);
        }
    }

    public void clearHistory() {
        mCompositeSubscription.add(mComicManager.listHistory()
                .flatMap(new Func1<List<Comic>, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(final List<Comic> list) {
                        return mComicManager.callInTx(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                for (Comic comic : list) {
                                    if (comic.getFavorite() == null && comic.getDownload() == null) {
                                        mComicManager.delete(comic);
                                    } else {
                                        comic.setHistory(null);
                                        mComicManager.update(comic);
                                    }
                                }
                                return null;
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mBaseView.onHistoryClearSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onHistoryClearFail();
                    }
                }));
    }

}
