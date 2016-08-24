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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MiniComic>>() {
                    @Override
                    public void call(List<MiniComic> list) {
                        mBaseView.onLoadSuccess(list);
                    }
                });
    }

    public void deleteHistory(MiniComic comic) {
        mComicManager.loadInRx(comic.getId())
                .observeOn(Schedulers.io())
                .subscribe(new Action1<Comic>() {
                    @Override
                    public void call(Comic comic) {
                        if (comic.getFavorite() == null) {
                            mComicManager.delete(comic);
                        } else {
                            comic.setHistory(null);
                            mComicManager.update(comic);
                        }
                    }
                });
    }

    public void clearHistory() {
        mComicManager.listHistory()
                .flatMap(new Func1<List<Comic>, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(final List<Comic> list) {
                        return mComicManager.callInTx(new Callable<Void>() {
                            @Override
                            public Void call() throws Exception {
                                for (Comic comic : list) {
                                    if (comic.getFavorite() == null) {
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
                        mBaseView.onHistoryClear();
                    }
                });
    }

}
