package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.global.SharedComic;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Selectable;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.ToAnotherList;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class DetailPresenter extends BasePresenter<DetailView> {

    private TaskManager mTaskManager;
    private TagManager mTagManager;
    private SharedComic mSharedComic;
    private Set<Long> mTagSet;

    public DetailPresenter() {
        mTaskManager = TaskManager.getInstance();
        mTagManager = TagManager.getInstance();
        mSharedComic = SharedComic.getInstance();
        mTagSet = new HashSet<>();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_CHAPTER_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onChapterChange(mSharedComic.last());
            }
        });
    }

    public void loadDetail(int source, String cid) {
        mSharedComic.open(source, cid);
        if (mSharedComic.get() == null) {
            mSharedComic.open(new Comic(source, cid));
        }
        loadDetail();
    }

    public void loadDetail(long id) {
        mSharedComic.open(id);
        loadDetail();
    }

    public void loadDetail() {
        if (mSharedComic.isHighLight()) {
            mSharedComic.get().setHighlight(false);
            mSharedComic.get().setFavorite(System.currentTimeMillis());
            mSharedComic.update();
        }
        mCompositeSubscription.add(Manga.info(mSharedComic.get())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Chapter>>() {
                    @Override
                    public void call(List<Chapter> list) {
                        mBaseView.onComicLoadSuccess(mSharedComic.get());
                        mBaseView.onChapterLoadSuccess(list);
                        mBaseView.onDetailLoadSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicLoadSuccess(mSharedComic.get());
                        if (throwable instanceof Manga.NetworkErrorException) {
                            mBaseView.onNetworkError();
                        } else {
                            mBaseView.onParseError();
                        }
                    }
                }));
    }

    public void loadTag() {
        mTagSet.clear();
        mCompositeSubscription.add(mTagManager.listByComic(mSharedComic.id())
                .compose(new ToAnotherList<>(new Func1<TagRef, Long>() {
                    @Override
                    public Long call(TagRef ref) {
                        return ref.getTid();
                    }
                }))
                .flatMap(new Func1<List<Long>, Observable<List<Tag>>>() {
                    @Override
                    public Observable<List<Tag>> call(List<Long> list) {
                        mTagSet.addAll(list);
                        return mTagManager.list();
                    }
                })
                .compose(new ToAnotherList<>(new Func1<Tag, Selectable>() {
                    @Override
                    public Selectable call(Tag tag) {
                        return new Selectable(false, mTagSet.contains(tag.getId()), tag.getId(), tag.getTitle());
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Selectable>>() {
                    @Override
                    public void call(List<Selectable> list) {
                        mBaseView.onTagLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTagLoadFail();
                    }
                }));
    }

    public void updateRef(final List<Long> insertList) {
        mCompositeSubscription.add(Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                final List<Long> deleteList = new LinkedList<>(mTagSet);
                deleteList.removeAll(insertList);
                insertList.removeAll(mTagSet);
                if (!deleteList.isEmpty() || !insertList.isEmpty()) {
                    mTagManager.runInTx(new Runnable() {
                        @Override
                        public void run() {
                            for (Long tid : deleteList) {
                                mTagManager.delete(tid, mSharedComic.id());
                            }
                            for (Long tid : insertList) {
                                mTagManager.insert(new TagRef(null, tid, mSharedComic.id()));
                            }
                        }
                    });
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TAG_UPDATE, mSharedComic.minify(), deleteList, insertList));
                }
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void v) {
                        mBaseView.onTagUpdateSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTagUpdateFail();
                    }
                }));
    }

    public void loadDownload() {
        if (mSharedComic.isDownload()) {
            mCompositeSubscription.add(mTaskManager.list(mSharedComic.id())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Task>>() {
                        @Override
                        public void call(List<Task> list) {
                            mBaseView.onDownloadLoadSuccess(list);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mBaseView.onDownloadLoadFail();
                        }
                    }));
        } else {
            mBaseView.onDownloadLoadSuccess(new LinkedList<Task>());
        }
    }

    public void updateLast(String last, boolean favorite) {
        if (favorite) {
            mSharedComic.get().setFavorite(System.currentTimeMillis());
        }
        mSharedComic.get().setHistory(System.currentTimeMillis());
        if (!last.equals(mSharedComic.get().getLast())) {
            mSharedComic.get().setLast(last);
            mSharedComic.get().setPage(1);
        }
        if (mSharedComic.id() != null) {
            mSharedComic.update();
        } else {
            mSharedComic.insert();
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_READ, mSharedComic.minify(), favorite));
    }

    public boolean isFavorite() {
        return mSharedComic.isFavorite();
    }

    public String getLast() {
        return mSharedComic.last();
    }

    public void favoriteComic() {
        mSharedComic.get().setFavorite(System.currentTimeMillis());
        mSharedComic.insert();
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_FAVORITE, mSharedComic.minify()));
    }

    public void unfavoriteComic() {
        long id = mSharedComic.id();
        mSharedComic.get().setFavorite(null);
        mTagManager.deleteByComic(id);
        mSharedComic.delete();
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_UNFAVORITE, id));
    }

}
