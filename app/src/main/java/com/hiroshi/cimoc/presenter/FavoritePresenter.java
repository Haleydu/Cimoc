package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
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
public class FavoritePresenter extends GridPresenter<FavoriteView> {

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
                mBaseView.onItemAdd((MiniComic) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_UNFAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onItemRemove((long) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_FAVORITE_RESTORE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onItemAdd((List<MiniComic>) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_REMOVE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onSourceRemove((int) rxEvent.getData());
            }
        });
    }

    @Override
    protected Observable<List<Comic>> getRawObservable() {
        return mComicManager.listFavorite();
    }

    private int size;

    public void checkUpdate() {
        mCompositeSubscription.add(mComicManager.listFavorite()
                .flatMap(new Func1<List<Comic>, Observable<Comic>>() {
                    @Override
                    public Observable<Comic> call(List<Comic> list) {
                        size = list.size();
                        return Manga.check(list);
                    }
                })
                .doOnNext(new Action1<Comic>() {
                    @Override
                    public void call(Comic comic) {
                        if (comic != null) {
                            mComicManager.update(comic);
                        }
                    }
                })
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Comic>() {
                    private int count = 0;

                    @Override
                    public void onCompleted() {
                        mBaseView.onCheckComplete();
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Comic comic) {
                        mBaseView.onComicUpdate(comic, ++count, size);
                    }
                }));
    }

    public void updateComic(long fromId, long toId, boolean isBack) {
        Comic fromComic = mComicManager.load(fromId);
        Comic toComic = mComicManager.load(toId);
        long favorite = isBack ? toComic.getFavorite() - 1 : toComic.getFavorite() + 1;
        fromComic.setFavorite(favorite);
        mComicManager.update(fromComic);
    }

}
