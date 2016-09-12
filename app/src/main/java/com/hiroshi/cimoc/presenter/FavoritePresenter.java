package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Source;
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
public class FavoritePresenter extends BasePresenter<FavoriteView> {

    private ComicManager mComicManager;
    private SourceManager mSourceManager;

    public FavoritePresenter() {
        mComicManager = ComicManager.getInstance();
        mSourceManager = SourceManager.getInstance();
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
        mComicManager.listFavorite()
                .flatMap(new Func1<List<Comic>, Observable<Comic>>() {
                    @Override
                    public Observable<Comic> call(List<Comic> list) {
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
                        if (comic != null) {
                            mBaseView.onComicUpdate(new MiniComic(comic));
                        }
                        mBaseView.onProgressChange(++count);
                    }
                });
    }

    public void updateComic(long fromId, long toId, boolean isBack) {
        Comic fromComic = mComicManager.load(fromId);
        Comic toComic = mComicManager.load(toId);
        if (isBack) {
            fromComic.setFavorite(toComic.getFavorite() - 1);
        } else {
            fromComic.setFavorite(toComic.getFavorite() + 1);
        }
        mComicManager.update(fromComic);
    }

    public void loadComic() {
        mComicManager.listFavorite()
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
                        mBaseView.onItemAdd(list);
                    }
                });
    }

    public void loadFilter() {
        mSourceManager.list()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Source>>() {
                    @Override
                    public void call(List<Source> list) {
                        String[] filter = new String[list.size() + 2];
                        filter[0] = "连载中";
                        filter[1] = "已完结";
                        for (int i = 0; i != list.size(); ++i) {
                            filter[i + 2] = SourceManager.getTitle(list.get(i).getSid());
                        }
                        mBaseView.onFilterLoad(filter);
                    }
                });
    }

}
