package com.hiroshi.cimoc.presenter;

import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.TagComicView;

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

public class TagComicPresenter extends BasePresenter<TagComicView> {

    private ComicManager mComicManager;
    private TagManager mTagManager;
    private Tag mTag;
    private LongSparseArray<MiniComic> mComicArray;

    public TagComicPresenter() {
        mComicManager = ComicManager.getInstance();
        mTagManager = TagManager.getInstance();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_UNFAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onComicUnFavorite((long) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_FAVORITE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mComicArray.remove(((MiniComic) rxEvent.getData()).getId());
            }
        });
        addSubscription(RxEvent.EVENT_TAG_UPDATE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                // Todo
                MiniComic comic = (MiniComic) rxEvent.getData();
                if (mComicArray.get(comic.getId()) != null) {
                    mComicArray.remove(comic.getId());
                    mBaseView.onTagComicInsert(comic);
                } else {
                    mComicArray.put(comic.getId(), comic);
                    mBaseView.onTagComicDelete(comic);
                }
            }
        });
    }

    public void loadTagComic(long id, String title) {
        mTag = new Tag(id, title);
        mCompositeSubscription.add(mTagManager.listByTag(id)
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
                })
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

    public void insert(long id) {
        mTagManager.insert(new TagRef(null, mTag.getId(), id));
    }

    public void insert(boolean[] check) {
        int size = mComicArray.size();
        List<TagRef> rList = new ArrayList<>();
        List<MiniComic> cList = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            if (check[i]) {
                MiniComic comic = mComicArray.valueAt(i);
                rList.add(new TagRef(null, mTag.getId(), comic.getId()));
                cList.add(comic);
                mComicArray.remove(comic.getId());
            }
        }
        mTagManager.insert(rList);
        mBaseView.onComicInsertSuccess(cList);
    }

    public void delete(long tid, long cid) {
        mTagManager.delete(tid, cid);
    }

}
