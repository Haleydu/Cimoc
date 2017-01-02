package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Backup;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.BackupView;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/10/19.
 */

public class BackupPresenter extends BasePresenter<BackupView> {

    private ComicManager mComicManager;
    private TagManager mTagManager;

    public BackupPresenter() {
        mComicManager = ComicManager.getInstance();
        mTagManager = TagManager.getInstance();
    }

    public void loadFavoriteFile() {
        mCompositeSubscription.add(Backup.loadFavorite()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String[]>() {
                    @Override
                    public void call(String[] file) {
                        mBaseView.onFavoriteFileLoadSuccess(file);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onFileLoadFail();
                    }
                }));
    }

    public void loadTagFile() {
        mCompositeSubscription.add(Backup.loadTag()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String[]>() {
                    @Override
                    public void call(String[] file) {
                        mBaseView.onTagFileLoadSuccess(file);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onFileLoadFail();
                    }
                }));
    }

    public void loadTag() {
        mCompositeSubscription.add(mTagManager.listInRx()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Tag>>() {
                    @Override
                    public void call(List<Tag> list) {
                        mBaseView.onTagLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupSaveFail();
                    }
                }));
    }

    public void saveFavorite() {
        mCompositeSubscription.add(mComicManager.listFavoriteInRx()
                .flatMap(new Func1<List<Comic>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(List<Comic> list) {
                        return Backup.saveFavorite(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer size) {
                        mBaseView.onBackupSaveSuccess(size);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupSaveFail();
                    }
                }));
    }

    public void saveTag(final long id, final String title) {
        mCompositeSubscription.add(Observable.create(new Observable.OnSubscribe<Integer>() {
                    @Override
                    public void call(Subscriber<? super Integer> subscriber) {
                        final List<TagRef> list = mTagManager.listByTag(id);
                        List<Comic> result = mComicManager.callInTx(new Callable<List<Comic>>() {
                            @Override
                            public List<Comic> call() throws Exception {
                                List<Comic> result = new LinkedList<>();
                                for (TagRef ref : list) {
                                    result.add(mComicManager.load(ref.getCid()));
                                }
                                return result;
                            }
                        });
                        int size = Backup.saveTag(new Tag(id, title), result);
                        if (size == -1) {
                            subscriber.onError(new Exception());
                        } else {
                            subscriber.onNext(size);
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer size) {
                        mBaseView.onBackupSaveSuccess(size);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupSaveFail();
                    }
                }));
    }

    public void restoreFavorite(String filename) {
        mCompositeSubscription.add(Backup.restoreFavorite(filename)
                .flatMap(new Func1<List<Comic>, Observable<List<MiniComic>>>() {
                    @Override
                    public Observable<List<MiniComic>> call(final List<Comic> list) {
                        return Observable.create(new Observable.OnSubscribe<List<MiniComic>>() {
                            @Override
                            public void call(Subscriber<? super List<MiniComic>> subscriber) {
                                List<MiniComic> result = updateComicList(list);
                                subscriber.onNext(result);
                                subscriber.onCompleted();
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MiniComic>>() {
                    @Override
                    public void call(List<MiniComic> list) {
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_FAVORITE_RESTORE, list));
                        mBaseView.onBackupRestoreSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupRestoreFail();
                    }
                }));
    }

    public void restoreTag(String filename) {
        mCompositeSubscription.add(Backup.restoreTag(filename)
                .flatMap(new Func1<Pair<String, List<Comic>>, Observable<Pair<Tag, List<MiniComic>>>>() {
                    @Override
                    public Observable<Pair<Tag, List<MiniComic>>> call(final Pair<String, List<Comic>> pair) {
                        return Observable.create(new Observable.OnSubscribe<Pair<Tag, List<MiniComic>>>() {
                            @Override
                            public void call(Subscriber<? super Pair<Tag, List<MiniComic>>> subscriber) {
                                Tag tag = loadOrInsertTag(pair.first);
                                List<MiniComic> list = updateComicList(pair.second);
                                insertTagRef(tag, pair.second);
                                subscriber.onNext(Pair.create(tag, list));
                                subscriber.onCompleted();
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair<Tag, List<MiniComic>>>() {
                    @Override
                    public void call(Pair<Tag, List<MiniComic>> pair) {
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TAG_RESTORE, pair.first));
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_FAVORITE_RESTORE, pair.second));
                        mBaseView.onBackupRestoreSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupRestoreFail();
                    }
                }));
    }

    private Tag loadOrInsertTag(final String title) {
        Tag tag = mTagManager.load(title);
        if (tag == null) {
            tag = new Tag(null, title);
            mTagManager.insert(tag);
        }
        return tag;
    }

    private void insertTagRef(final Tag tag, final List<Comic> list) {
        mTagManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (Comic comic : list) {
                    TagRef ref = mTagManager.load(tag.getId(), comic.getId());
                    if (ref == null) {
                        mTagManager.insert(new TagRef(null, tag.getId(), comic.getId()));
                    }
                }
            }
        });
    }

    private List<MiniComic> updateComicList(final List<Comic> list) {
        return mComicManager.callInTx(new Callable<List<MiniComic>>() {
            @Override
            public List<MiniComic> call() throws Exception {
                List<MiniComic> result = new LinkedList<>();
                long favorite = System.currentTimeMillis() + list.size() * 10;
                for (Comic comic : list) {
                    Comic temp = mComicManager.load(comic.getSource(), comic.getCid());
                    if (temp == null) {
                        comic.setFavorite(favorite);
                        mComicManager.insert(comic);
                        result.add(new MiniComic(comic));
                    } else {
                        if (temp.getFavorite() == null) {
                            temp.setFavorite(favorite);
                            mComicManager.update(temp);
                            result.add(new MiniComic(temp));
                        }
                        comic.setId(temp.getId());
                    }
                    favorite -= 20;
                }
                return result;
            }
        });
    }

}
