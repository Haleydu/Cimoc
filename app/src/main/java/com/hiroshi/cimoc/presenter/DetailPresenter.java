package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Selectable;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.fragment.ComicFragment;
import com.hiroshi.cimoc.ui.view.DetailView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

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

    private ComicManager mComicManager;
    private TaskManager mTaskManager;
    private TagManager mTagManager;
    private Comic mComic;
    private Set<Long> mTagSet;

    public DetailPresenter() {
        mComicManager = ComicManager.getInstance();
        mTaskManager = TaskManager.getInstance();
        mTagManager = TagManager.getInstance();
        mTagSet = new HashSet<>();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_COMIC_CHAPTER_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                String last = (String) rxEvent.getData();
                int page = (int) rxEvent.getData(1);
                mComic.setHistory(System.currentTimeMillis());
                mComic.setLast(last);
                mComic.setPage(page);
                mBaseView.onChapterChange(last);
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_PAGE_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mComic.setPage((Integer) rxEvent.getData());
                if (mComic.getId() != null) {
                    mComicManager.update(mComic);
                }
            }
        });
    }

    public void loadDetail(long id, final int source, final String cid) {
        mComic = id == -1 ? mComicManager.load(source, cid) : mComicManager.load(id);
        if (mComic == null) {
            mComic = new Comic(source, cid);
        } if (mComic.getHighlight()) {
            mComic.setHighlight(false);
            mComic.setFavorite(System.currentTimeMillis());
            mComicManager.update(mComic);
        }
        mCompositeSubscription.add(Manga.info(source, mComic)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Chapter>>() {
                    @Override
                    public void call(List<Chapter> list) {
                        mBaseView.onComicLoadSuccess(mComic);
                        mBaseView.onChapterLoadSuccess(list);
                        mBaseView.onDetailLoadSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicLoadSuccess(mComic);
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
        mCompositeSubscription.add(mTagManager.listByComic(mComic.getId())
                .flatMap(new Func1<List<TagRef>, Observable<TagRef>>() {
                    @Override
                    public Observable<TagRef> call(List<TagRef> tagRefs) {
                        return Observable.from(tagRefs);
                    }
                })
                .map(new Func1<TagRef, Long>() {
                    @Override
                    public Long call(TagRef ref) {
                        return ref.getTid();
                    }
                })
                .toList()
                .flatMap(new Func1<List<Long>, Observable<List<Tag>>>() {
                    @Override
                    public Observable<List<Tag>> call(List<Long> list) {
                        mTagSet.addAll(list);
                        return mTagManager.list();
                    }
                })
                .flatMap(new Func1<List<Tag>, Observable<Tag>>() {
                    @Override
                    public Observable<Tag> call(List<Tag> list) {
                        return Observable.from(list);
                    }
                })
                .map(new Func1<Tag, Selectable>() {
                    @Override
                    public Selectable call(Tag tag) {
                        return new Selectable(false, mTagSet.contains(tag.getId()), tag.getId(), tag.getTitle());
                    }
                })
                .toList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Selectable>>() {
                    @Override
                    public void call(List<Selectable> list) {
                        mBaseView.onTagLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
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
                                mTagManager.delete(tid, mComic.getId());
                            }
                            for (Long tid : insertList) {
                                mTagManager.insert(new TagRef(null, tid, mComic.getId()));
                            }
                        }
                    });
                    RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TAG_UPDATE, new MiniComic(mComic), deleteList, insertList));
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
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

    public void updateIndex(List<Chapter> list) {
        mCompositeSubscription.add(Download.updateComicIndex(list, mComic.getSource(), mComic.getCid(), mComic.getTitle(), mComic.getCover())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mBaseView.onUpdateIndexSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onUpdateIndexFail();
                    }
                }));
    }

    public void loadDownload() {
        if (mComic.getDownload() != null) {
            mCompositeSubscription.add(mTaskManager.list(mComic.getId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<List<Task>>() {
                        @Override
                        public void call(List<Task> list) {
                            Set<String> set = new HashSet<>();
                            for (Task task : list) {
                                if (task.isFinish()) {
                                    set.add(task.getPath());
                                }
                            }
                            mBaseView.onDownloadLoadSuccess(set);
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            mBaseView.onDownloadLoadFail();
                        }
                    }));
        } else {
            mBaseView.onDownloadLoadSuccess(new HashSet<String>());
        }
    }

    public void addTask(final List<Chapter> list) {
        mCompositeSubscription.add(mComicManager.callInRx(new Callable<ArrayList<Task>>() {
            @Override
            public ArrayList<Task> call() throws Exception {
                Long key = mComic.getId();
                mComic.setDownload(System.currentTimeMillis());
                if (key != null) {
                    mComicManager.update(mComic);
                } else {
                    key = mComicManager.insert(mComic);
                    mComic.setId(key);
                }
                ArrayList<Task> taskList = new ArrayList<>(list.size());
                for (Chapter chapter : list) {
                    Task task = new Task(null, key, chapter.getPath(), chapter.getTitle(), 0, 0);
                    long id = mTaskManager.insert(task);
                    task.setId(id);
                    task.setInfo(mComic.getSource(), mComic.getCid(), mComic.getTitle());
                    task.setState(Task.STATE_WAIT);
                    taskList.add(task);
                }
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_INSERT, new MiniComic(mComic), taskList));
                return taskList;
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ArrayList<Task>>() {
                    @Override
                    public void call(ArrayList<Task> list) {
                        mBaseView.onTaskAddSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTaskAddFail();
                    }
                }));
    }

    public long updateLast(String last, int type) {
        if (type == ComicFragment.TYPE_FAVORITE) {
            mComic.setFavorite(System.currentTimeMillis());
        }
        mComic.setHistory(System.currentTimeMillis());
        if (!last.equals(mComic.getLast())) {
            mComic.setLast(last);
            mComic.setPage(1);
        }
        if (mComic.getId() != null) {
            mComicManager.update(mComic);
        } else {
            long id = mComicManager.insert(mComic);
            mComic.setId(id);
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_READ, new MiniComic(mComic), type));
        return mComic.getId();
    }

    public void updateComic() {
        if (mComic.getId() != null) {
            mComicManager.update(mComic);
        }
    }

    public Comic getComic() {
        return mComic;
    }

    public void favoriteComic() {
        mComic.setFavorite(System.currentTimeMillis());
        if (mComic.getId() == null) {
            long id = mComicManager.insert(mComic);
            mComic.setId(id);
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_FAVORITE, new MiniComic(mComic)));
    }

    public void unfavoriteComic() {
        long id = mComic.getId();
        mComic.setFavorite(null);
        mTagManager.deleteByComic(mComic.getId());
        if (mComic.getHistory() == null && mComic.getDownload() == null) {
            mComicManager.deleteByKey(id);
            mComic.setId(null);
        }
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_UNFAVORITE, id));
    }

}
