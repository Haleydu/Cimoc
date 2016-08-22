package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.FavoriteView;

import java.util.LinkedList;
import java.util.List;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
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

    public void checkUpdate() {
        List<MiniComic> favorite = mComicManager.listFavorite();
        final int size = favorite.size();
        Manga.check(favorite)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<MiniComic>() {
                    List<MiniComic> list = new LinkedList<>();
                    int count = 0;

                    @Override
                    public void onCompleted() {
                        mComicManager.updateFavorite(list);
                        mBaseView.onCheckComplete(list);
                    }

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(MiniComic comic) {
                        if (comic != null) {
                            list.add(comic);
                        }
                        mBaseView.onProgressChange(++count, size);
                    }
                });
    }

    public List<MiniComic> getComicList() {
        return mComicManager.listFavorite();
    }

    public void deleteComic(MiniComic comic) {
        mComicManager.deleteFavorite(comic.getId());
    }

}
