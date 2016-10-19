package com.hiroshi.cimoc.presenter;

import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.fragment.ComicFragment;
import com.hiroshi.cimoc.ui.view.FavoriteView;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
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
    private TagManager mTagManager;
    private LongSparseArray<MiniComic> mComicArray;
    private long mTagId = -1;
    private Comparator<MiniComic> mComparator;

    public FavoritePresenter() {
        mComicManager = ComicManager.getInstance();
        mTagManager = TagManager.getInstance();
        mComicArray = new LongSparseArray<>();
        mComparator = new Comparator<MiniComic>() {
            @Override
            public int compare(MiniComic o1, MiniComic o2) {
                if (o1.isHighlight() && !o2.isHighlight()) {
                    return -1;
                } else if (!o1.isHighlight() && o2.isHighlight()) {
                    return 1;
                } else {
                    return o1.getFavorite() - o2.getFavorite() > 0L ? -1 : 1;
                }
            }
        };
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
                if ((int) rxEvent.getData(1) == ComicFragment.TYPE_FAVORITE) {
                    MiniComic comic = (MiniComic) rxEvent.getData();
                    mComicArray.put(comic.getId(), comic);
                    mBaseView.onComicRead(comic);
                }
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_FILTER, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                int type = (int) rxEvent.getData();
                if (type == TagManager.TAG_NORMAL) {
                    mTagId = (long) rxEvent.getData(1);
                }
                filter(type);
            }
        });
        addSubscription(RxEvent.EVENT_THEME_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onThemeChange((int) rxEvent.getData(1), (int) rxEvent.getData(2));
            }
        });
        addSubscription(RxEvent.EVENT_TAG_UPDATE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                List<Long> deleteList = (List<Long>) rxEvent.getData(1);
                List<Long> insertList = (List<Long>) rxEvent.getData(2);
                if (deleteList.contains(mTagId)) {
                    mBaseView.OnComicUnFavorite(((MiniComic) rxEvent.getData()).getId());
                } else if (insertList.contains(mTagId)) {
                    mBaseView.OnComicFavorite((MiniComic) rxEvent.getData());
                }
            }
        });
    }

    public void loadComic() {
        mCompositeSubscription.add(mComicManager.listFavorite()
                .flatMap(new Func1<List<Comic>, Observable<Comic>>() {
                    @Override
                    public Observable<Comic> call(List<Comic> list) {
                        return Observable.from(list);
                    }
                })
                .map(new Func1<Comic, MiniComic>() {
                    @Override
                    public MiniComic call(Comic comic) {
                        MiniComic ret = new MiniComic(comic);
                        mComicArray.put(ret.getId(), ret);
                        return ret;
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

    private void filter(int type) {
        if (type == TagManager.TAG_NORMAL) {
            mCompositeSubscription.add(mTagManager.listByTag(mTagId)
                    .flatMap(new Func1<List<TagRef>, Observable<TagRef>>() {
                        @Override
                        public Observable<TagRef> call(List<TagRef> tagRefs) {
                            return Observable.from(tagRefs);
                        }
                    })
                    .map(new Func1<TagRef, MiniComic>() {
                        @Override
                        public MiniComic call(TagRef ref) {
                            return mComicArray.get(ref.getCid());
                        }
                    })
                    .filter(new Func1<MiniComic, Boolean>() {
                        @Override
                        public Boolean call(MiniComic comic) {
                            return comic != null;
                        }
                    })
                    .toList()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<MiniComic>>() {
                        @Override
                        public void call(List<MiniComic> list) {
                            Collections.sort(list, mComparator);
                            mBaseView.onComicFilterSuccess(list);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mBaseView.onComicFilterFail();
                        }
                    }));
        } else {
            List<MiniComic> list = new LinkedList<>();
            for (int i = 0; i != mComicArray.size(); ++i) {
                if (type != TagManager.TAG_ALL) {
                    Boolean finish = mComicArray.valueAt(i).isFinish();
                    if (type == TagManager.TAG_CONTINUE && (finish == null || !finish) ||
                            type == TagManager.TAG_END && finish != null && finish) {
                        list.add(mComicArray.valueAt(i));
                    }
                } else {
                    list.add(mComicArray.valueAt(i));
                }
            }
            Collections.sort(list, mComparator);
            mBaseView.onComicFilterSuccess(list);
        }
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

}
