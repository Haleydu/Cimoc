package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.TagComicView;

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
                mBaseView.onComicFavorite((MiniComic) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_TAG_UPDATE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                List<Long> deleteList = (List<Long>) rxEvent.getData(1);
                List<Long> insertList = (List<Long>) rxEvent.getData(2);
                if (deleteList.contains(mTag.getId())) {
                    mBaseView.onTagUpdateDelete((MiniComic) rxEvent.getData());
                } else if (insertList.contains(mTag.getId())) {
                    mBaseView.onTagUpdateInsert((MiniComic) rxEvent.getData());
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MiniComic>>() {
                    @Override
                    public void call(List<MiniComic> list) {
                        mBaseView.onTagComicLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTagComicLoadFail();
                    }
                }));
    }

    public void loadComic(final Set<MiniComic> set) {
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
                        return new MiniComic(comic);
                    }
                })
                .filter(new Func1<MiniComic, Boolean>() {
                    @Override
                    public Boolean call(MiniComic comic) {
                        return !set.contains(comic);
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

    public void insert(long tid, long cid) {
        mTagManager.insert(new TagRef(null, tid, cid));
    }

    public void insert(final long tid, List<MiniComic> list) {
        Observable.from(list)
                .map(new Func1<MiniComic, TagRef>() {
                    @Override
                    public TagRef call(MiniComic comic) {
                        return new TagRef(null, tid, comic.getId());
                    }
                })
                .toList()
                .subscribe(new Action1<List<TagRef>>() {
                    @Override
                    public void call(List<TagRef> tagRefs) {
                        mTagManager.insert(tagRefs);
                        mBaseView.onComicInsertSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicInsertFail();
                    }
                });
    }

    public void delete(long tid, long cid) {
        mTagManager.delete(tid, cid);
    }

}
