package com.hiroshi.cimoc.presenter;

import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.ToAnotherList;
import com.hiroshi.cimoc.ui.view.PartFavoriteView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class PartFavoritePresenter extends BasePresenter<PartFavoriteView> {

    private ComicManager mComicManager;
    private TagManager mTagManager;
    private Tag mTag;
    private LongSparseArray<MiniComic> mComicArray;

    public PartFavoritePresenter() {
        mComicManager = ComicManager.getInstance();
        mTagManager = TagManager.getInstance();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_UNFAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onComicRemove((long) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_TAG_UPDATE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                long id = (long) rxEvent.getData();
                List<Long> dList = (List<Long>) rxEvent.getData(1);
                List<Long> iList = (List<Long>) rxEvent.getData(2);
                if (dList.contains(mTag.getId())) {
                    MiniComic comic = new MiniComic(mComicManager.load(id));
                    mComicArray.put(id, comic);
                    mBaseView.onComicRemove(id);
                } else if (iList.contains(mTag.getId())) {
                    MiniComic comic = mComicArray.get(id);
                    mComicArray.remove(id);
                    mBaseView.onComicAdd(comic);
                }
            }
        });
    }

    private Observable<List<MiniComic>> getObservable(long id) {
        if (id == TagManager.TAG_CONTINUE) {
            return mComicManager.listContinueInRx()
                    .compose(new ToAnotherList<>(new Func1<Comic, MiniComic>() {
                        @Override
                        public MiniComic call(Comic comic) {
                            return new MiniComic(comic);
                        }
                    }));
        } else if (id == TagManager.TAG_FINISH) {
            return mComicManager.listFinishInRx()
                    .compose(new ToAnotherList<>(new Func1<Comic, MiniComic>() {
                        @Override
                        public MiniComic call(Comic comic) {
                            return new MiniComic(comic);
                        }
                    }));
        } else {
            return mTagManager.listByTagInRx(id)
                    .flatMap(new Func1<List<TagRef>, Observable<List<MiniComic>>>() {
                        @Override
                        public Observable<List<MiniComic>> call(final List<TagRef> tagRefs) {
                            return mComicManager.callInRx(new Callable<List<MiniComic>>() {
                                @Override
                                public List<MiniComic> call() throws Exception {
                                    List<MiniComic> list = new LinkedList<>();
                                    for (TagRef ref : tagRefs) {
                                        list.add(new MiniComic(mComicManager.load(ref.getCid())));
                                    }
                                    return list;
                                }
                            });
                        }
                    })
                    .doOnNext(new Action1<List<MiniComic>>() {
                        @Override
                        public void call(List<MiniComic> list) {
                            mComicArray = new LongSparseArray<>();
                            Set<MiniComic> set = new HashSet<>(list);
                            for (Comic comic : mComicManager.listFavorite()) {
                                MiniComic mc = new MiniComic(comic);
                                if (!set.contains(mc)) {
                                    mComicArray.put(mc.getId(), mc);
                                }
                            }
                        }
                    });
        }
    }

    public void load(long id, String title) {
        mTag = new Tag(id, title);
        mCompositeSubscription.add(getObservable(id)
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

    public List<MiniComic> getComicList() {
        List<MiniComic> list = new ArrayList<>();
        int size = mComicArray.size();
        for (int i = 0; i < size; ++i) {
            list.add(mComicArray.valueAt(i));
        }
        return list;
    }

    public void insert(List<Long> list) {
        List<TagRef> rList = new ArrayList<>();
        List<MiniComic> cList = new ArrayList<>();
        for (Long id : list) {
            MiniComic comic = mComicArray.get(id);
            if (comic != null) {
                rList.add(new TagRef(null, mTag.getId(), comic.getId()));
                cList.add(comic);
                mComicArray.remove(comic.getId());
            }
        }
        mTagManager.insertInTx(rList);
        mBaseView.onComicInsertSuccess(cList);
    }

    public void delete(long tid, long cid) {
        mTagManager.delete(tid, cid);
    }

}
