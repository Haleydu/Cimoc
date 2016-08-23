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
import rx.schedulers.Schedulers;

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
        addSubscription(RxEvent.FAVORITE_COMIC, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onItemAdd((MiniComic) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.UN_FAVORITE_COMIC, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onItemRemove((long) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.RESTORE_FAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onItemAdd((List<MiniComic>) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.COMIC_DELETE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onSourceRemove((int) rxEvent.getData());
            }
        });
    }

    private int size;
    private int count;

    public void checkUpdate() {
        mComicManager.listFavorite()
                .flatMap(new Func1<List<Comic>, Observable<Comic>>() {
                    @Override
                    public Observable<Comic> call(List<Comic> list) {
                        count = 0;
                        size = list.size();
                        return Manga.check(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Comic>() {
                    @Override
                    public void onCompleted() {
                        mBaseView.onCheckComplete();
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(Comic comic) {
                        if (comic != null) {
                            mComicManager.update(comic);
                            mBaseView.onComicUpdate(new MiniComic(comic));
                        }
                        mBaseView.onProgressChange(++count, size);
                    }
                });
    }

    public void updateComic(long fromId, long toId, final boolean isBack) {
        Observable.concat(mComicManager.load(fromId), mComicManager.load(toId))
                .observeOn(Schedulers.io())
                .toList()
                .subscribe(new Action1<List<Comic>>() {
                    @Override
                    public void call(List<Comic> list) {
                        Comic fromComic = list.get(0);
                        Comic toComic = list.get(1);
                        if (isBack) {
                            fromComic.setFavorite(toComic.getFavorite() - 1);
                        } else {
                            fromComic.setFavorite(toComic.getFavorite() + 1);
                        }
                        mComicManager.update(fromComic);
                    }
                });
    }

    public void loadComic() {
        mComicManager.listFavorite()
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
                        mBaseView.onItemAdd(list);
                    }
                });
    }

}
