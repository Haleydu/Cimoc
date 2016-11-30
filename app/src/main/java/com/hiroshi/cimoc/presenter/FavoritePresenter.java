package com.hiroshi.cimoc.presenter;

import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.RxObject;
import com.hiroshi.cimoc.rx.ToAnotherList;
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
                if (mTagId == -1) {
                    mBaseView.OnComicRestore(list);
                }
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
        addSubscription(RxEvent.EVENT_COMIC_FILTER, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                int type = (int) rxEvent.getData();
                mTagId = type == TagManager.TAG_NORMAL ? (long) rxEvent.getData(1): -1;
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
                if (mTagId != -1) {
                    List<Long> deleteList = (List<Long>) rxEvent.getData(1);
                    List<Long> insertList = (List<Long>) rxEvent.getData(2);
                    if (deleteList.contains(mTagId)) {
                        mBaseView.onComicDelete((MiniComic) rxEvent.getData());
                    } else if (insertList.contains(mTagId)) {
                        mBaseView.onComicInsert((MiniComic) rxEvent.getData());
                    }
                }
            }
        });
    }

    public void loadComic() {
        mCompositeSubscription.add(mComicManager.listFavorite()
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

    private void filter(int type) {
        if (type == TagManager.TAG_NORMAL) {
            mCompositeSubscription.add(mTagManager.listByTag(mTagId)
                    .compose(new ToAnotherList<>(new Func1<TagRef, MiniComic>() {
                        @Override
                        public MiniComic call(TagRef ref) {
                            return mComicArray.get(ref.getCid());
                        }
                    }))
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

    public void checkUpdate() {
        mCompositeSubscription.add(mComicManager.listFavorite()
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
