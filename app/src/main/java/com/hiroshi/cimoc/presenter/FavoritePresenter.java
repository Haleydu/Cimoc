package com.hiroshi.cimoc.presenter;

import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.RxObject;
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
    private LongSparseArray<MiniComic> mComicArray;

    public FavoritePresenter() {
        mComicManager = ComicManager.getInstance();
        mComicArray = new LongSparseArray<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        super.initSubscription();
        addSubscription(RxEvent.EVENT_COMIC_FAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                MiniComic comic = (MiniComic) rxEvent.getData();
                mComicArray.put(comic.getId(), comic);
                mBaseView.OnComicFavorite(comic);
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_UNFAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                long id = (long) rxEvent.getData();
                mComicArray.remove(id);
                mBaseView.OnComicUnFavorite(id);
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_FAVORITE_RESTORE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                List<MiniComic> list = (List<MiniComic>) rxEvent.getData();
                for (MiniComic comic : list) {
                    mComicArray.put(comic.getId(), comic);
                }
                mBaseView.OnComicRestore(list);
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_READ, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                if ((boolean) rxEvent.getData(1)) {
                    MiniComic comic = (MiniComic) rxEvent.getData();
                    mComicArray.put(comic.getId(), comic);
                    mBaseView.onComicRead(comic);
                }
            }
        });
    }

    public void loadComic() {
        mCompositeSubscription.add(mComicManager.listFavoriteInRx()
                .compose(new ToAnotherList<>(new Func1<Comic, MiniComic>() {
                    @Override
                    public MiniComic call(Comic comic) {
                        MiniComic ret = new MiniComic(comic);
                        mComicArray.put(ret.getId(), ret);
                        return ret;
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
                .flatMap(new Func1<List<Comic>, Observable<RxObject>>() {
                    @Override
                    public Observable<RxObject> call(List<Comic> list) {
                        return Manga.check(list);
                    }
                })
                .doOnNext(new Action1<RxObject>() {
                    @Override
                    public void call(RxObject object) {
                        if (object.getData() != null) {
                            mComicManager.update((Comic) object.getData());
                        }
                    }
                })
                .onBackpressureBuffer()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RxObject>() {
                    @Override
                    public void onCompleted() {
                        mBaseView.onComicCheckComplete();
                    }

                    @Override
                    public void onError(Throwable e) {
                        mBaseView.onComicCheckFail();
                    }

                    @Override
                    public void onNext(RxObject object) {
                        if (object.getData() == null) {
                            mBaseView.onComicCheckSuccess(null, (int) object.getData(1), (int) object.getData(2));
                        } else {
                            long id = ((Comic) object.getData()).getId();
                            mComicArray.get(id).setHighlight(true);
                            mBaseView.onComicCheckSuccess(mComicArray.get(id), (int) object.getData(1), (int) object.getData(2));
                        }
                    }
                }));
    }

}
