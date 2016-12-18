package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.ToAnotherList;
import com.hiroshi.cimoc.ui.view.FavoriteView;

import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/7/6.
 */
public class FavoritePresenter extends BasePresenter<FavoriteView> {

    private ComicManager mComicManager;

    public FavoritePresenter() {
        mComicManager = ComicManager.getInstance();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        super.initSubscription();
        addSubscription(RxEvent.EVENT_COMIC_FAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                MiniComic comic = (MiniComic) rxEvent.getData();
                mBaseView.OnComicFavorite(comic);
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_UNFAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.OnComicUnFavorite((long) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_FAVORITE_RESTORE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.OnComicRestore((List<MiniComic>) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_READ, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                if ((boolean) rxEvent.getData(1)) {
                    MiniComic comic = (MiniComic) rxEvent.getData();
                    mBaseView.onComicRead(comic);
                }
            }
        });
    }

    public void load() {
        mCompositeSubscription.add(mComicManager.listFavoriteInRx()
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

    public void checkUpdate() {
        mCompositeSubscription.add(mComicManager.listFavoriteInRx()
                .flatMap(new Func1<List<Comic>, Observable<Pair<Comic, Pair<Integer, Integer>>>>() {
                    @Override
                    public Observable<Pair<Comic, Pair<Integer, Integer>>> call(List<Comic> list) {
                        return Manga.checkUpdate(list);
                    }
                })
                .doOnNext(new Action1<Pair<Comic, Pair<Integer, Integer>>>() {
                    @Override
                    public void call(Pair<Comic, Pair<Integer, Integer>> pair) {
                        if (pair.first != null) {
                            mComicManager.update(pair.first);
                        }
                    }
                })
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Pair<Comic, Pair<Integer, Integer>>>() {
                    @Override
                    public void onCompleted() {
                        mBaseView.onComicCheckComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mBaseView.onComicCheckFail();
                    }

                    @Override
                    public void onNext(Pair<Comic, Pair<Integer, Integer>> pair) {
                        MiniComic comic = pair.first == null ? null : new MiniComic(pair.first);
                        mBaseView.onComicCheckSuccess(comic, pair.second.first, pair.second.second);
                    }
                }));
    }

}
