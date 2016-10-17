package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.HistoryView;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

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
        super.initSubscription();
        addSubscription(RxEvent.EVENT_COMIC_READ, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onItemUpdate((MiniComic) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_THEME_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onThemeChange((int) rxEvent.getData(1), (int) rxEvent.getData(2));
            }
        });
    }

    public void loadComic() {
        mCompositeSubscription.add(mComicManager.listHistory()
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
                        mBaseView.onComicLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicLoadFail();
                    }
                }));
    }

    public void delete(MiniComic history) {
        Comic comic = mComicManager.load(history.getId());
        if (comic.getFavorite() == null && comic.getDownload() == null) {
            mComicManager.delete(comic);
        } else {
            comic.setHistory(null);
            mComicManager.update(comic);
        }
    }

    public void clear() {
        mCompositeSubscription.add(mComicManager.listHistory()
                .flatMap(new Func1<List<Comic>, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(final List<Comic> list) {
                        return mComicManager.runInRx(new Runnable() {
                            @Override
                            public void run() {
                                for (Comic comic : list) {
                                    if (comic.getFavorite() == null && comic.getDownload() == null) {
                                        mComicManager.delete(comic);
                                    } else {
                                        comic.setHistory(null);
                                        mComicManager.update(comic);
                                    }
                                }
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void v) {
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
