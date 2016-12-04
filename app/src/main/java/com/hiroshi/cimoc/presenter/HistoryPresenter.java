package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.ToAnotherList;
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
    }

    public void load() {
        mCompositeSubscription.add(mComicManager.listHistoryInRx()
                .compose(new ToAnotherList<>(new Func1<Comic, MiniComic>() {
                    @Override
                    public MiniComic call(Comic comic) {
                        return new MiniComic(comic);
                    }
                }))
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
        comic.setHistory(null);
        mComicManager.updateOrDelete(comic);
    }

    public void clear() {
        mCompositeSubscription.add(mComicManager.listHistoryInRx()
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
