package com.haleydu.cimoc.presenter;

import android.content.ContentResolver;
import android.util.Pair;

import com.haleydu.cimoc.App;
import com.haleydu.cimoc.core.Backup;
import com.haleydu.cimoc.manager.ComicManager;
import com.haleydu.cimoc.manager.TagManager;
import com.haleydu.cimoc.manager.TagRefManager;
import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.model.MiniComic;
import com.haleydu.cimoc.model.Tag;
import com.haleydu.cimoc.model.TagRef;
import com.haleydu.cimoc.rx.RxBus;
import com.haleydu.cimoc.rx.RxEvent;
import com.haleydu.cimoc.ui.view.BackupView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private TagRefManager mTagRefManager;
    private ContentResolver mContentResolver;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mTagManager = TagManager.getInstance(mBaseView);
        mTagRefManager = TagRefManager.getInstance(mBaseView);
        mContentResolver = mBaseView.getAppInstance().getContentResolver();
    }

    public void loadComicFile() {
        mCompositeSubscription.add(Backup.loadFavorite(mBaseView.getAppInstance().getDocumentFile())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String[]>() {
                    @Override
                    public void call(String[] file) {
                        mBaseView.onComicFileLoadSuccess(file);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onFileLoadFail();
                    }
                }));
    }

    public void loadTagFile() {
        mCompositeSubscription.add(Backup.loadTag(mBaseView.getAppInstance().getDocumentFile())
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

    public void loadSettingsFile() {
        mCompositeSubscription.add(Backup.loadSettings(mBaseView.getAppInstance().getDocumentFile())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String[]>() {
                    @Override
                    public void call(String[] file) {
                        mBaseView.onSettingsFileLoadSuccess(file);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onFileLoadFail();
                    }
                }));
    }

    public void loadClearBackupFile() {
        mCompositeSubscription.add(Backup.loadClearBackup(mBaseView.getAppInstance().getDocumentFile())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String[]>() {
                    @Override
                    public void call(String[] file) {
                        mBaseView.onClearFileLoadSuccess(file);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onFileLoadFail();
                    }
                }));
    }

    public void saveComic() {
        mCompositeSubscription.add(mComicManager.listFavoriteOrHistoryInRx()
                .map(new Func1<List<Comic>, Integer>() {
                    @Override
                    public Integer call(List<Comic> list) {
                        return Backup.saveComic(mContentResolver, mBaseView.getAppInstance().getDocumentFile(), list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer size) {
                        if (size == -1) {
                            mBaseView.onBackupSaveFail();
                        } else {
                            mBaseView.onBackupSaveSuccess(size);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupSaveFail();
                    }
                }));
    }

    public void saveTag() {
        mCompositeSubscription.add(Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                int size = groupAndSaveComicByTag();
                subscriber.onNext(size);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer size) {
                        if (size == -1) {
                            mBaseView.onBackupSaveFail();
                        } else {
                            mBaseView.onBackupSaveSuccess(size);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupSaveFail();
                    }
                }));
    }

    public void saveSettings() {
        mCompositeSubscription.add(Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
//                boolean isBackuped = SharePrefUtils.copyFiles(context.getFilesDir().getPath() + context.getPackageName() + "/shared_prefs", );
                int size = Backup.saveSetting(mContentResolver, mBaseView.getAppInstance().getDocumentFile(), App.getPreferenceManager().getAll());
                subscriber.onNext(size);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer size) {
                        if (size == -1) {
                            mBaseView.onBackupSaveFail();
                        } else {
                            mBaseView.onBackupSaveSuccess(size);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupSaveFail();
                    }
                }));
    }

    public void restoreComic(String filename) {
        mCompositeSubscription.add(Backup.restoreComic(mContentResolver, mBaseView.getAppInstance().getDocumentFile(), filename)
                .doOnNext(new Action1<List<Comic>>() {
                    @Override
                    public void call(List<Comic> list) {
                        filterAndPostComic(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Comic>>() {
                    @Override
                    public void call(List<Comic> list) {
                        mBaseView.onBackupRestoreSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        mBaseView.onBackupRestoreFail();
                    }
                }));
    }

    public void restoreTag(String filename) {
        mCompositeSubscription.add(Backup.restoreTag(mContentResolver, mBaseView.getAppInstance().getDocumentFile(), filename)
                .doOnNext(new Action1<List<Pair<Tag, List<Comic>>>>() {
                    @Override
                    public void call(List<Pair<Tag, List<Comic>>> list) {
                        updateAndPostTag(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Pair<Tag, List<Comic>>>>() {
                    @Override
                    public void call(List<Pair<Tag, List<Comic>>> pair) {
                        mBaseView.onBackupRestoreSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupRestoreFail();
                    }
                }));
    }

    public void restoreSetting(String filename) {
        mCompositeSubscription.add(Backup.restoreSetting(mContentResolver, mBaseView.getAppInstance().getDocumentFile(), filename)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Map<String, ?>>() {
                    @Override
                    public void call(Map<String, ?> pair) {
                        mBaseView.onBackupRestoreSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupRestoreFail();
                    }
                }));
    }

    public void clearBackup() {
        mCompositeSubscription.add(Backup.clearBackup(mBaseView.getAppInstance().getDocumentFile())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer pair) {
                        mBaseView.onClearBackupSuccess();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onClearBackupFail();
                    }
                }));
    }

    private List<Tag> setTagsId(final List<Pair<Tag, List<Comic>>> list) {
        final List<Tag> tags = new LinkedList<>();
        mTagRefManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (Pair<Tag, List<Comic>> pair : list) {
                    Tag tag = mTagManager.load(pair.first.getTitle());
                    if (tag == null) {
                        mTagManager.insert(pair.first);
                        tags.add(pair.first);
                    } else {
                        pair.first.setId(tag.getId());
                    }
                }
            }
        });
        return tags;
    }

    private void updateAndPostTag(final List<Pair<Tag, List<Comic>>> list) {
        List<Tag> tags = setTagsId(list);
        for (Pair<Tag, List<Comic>> pair : list) {
            filterAndPostComic(pair.second);
        }
        mTagRefManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (Pair<Tag, List<Comic>> pair : list) {
                    long tid = pair.first.getId();
                    for (Comic comic : pair.second) {
                        TagRef ref = mTagRefManager.load(tid, comic.getId());
                        if (ref == null) {
                            mTagRefManager.insert(new TagRef(null, tid, comic.getId()));
                        }
                    }
                }
            }
        });
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TAG_RESTORE, tags));
    }

    private int groupAndSaveComicByTag() {
        final List<Pair<Tag, List<Comic>>> list = new LinkedList<>();
        mComicManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (Tag tag : mTagManager.list()) {
                    List<Comic> comics = new LinkedList<>();
                    Pair<Tag, List<Comic>> pair = Pair.create(tag, comics);
                    for (TagRef ref : mTagRefManager.listByTag(tag.getId())) {
                        comics.add(mComicManager.load(ref.getCid()));
                    }
                    list.add(pair);
                }
            }
        });
        return Backup.saveTag(mContentResolver, mBaseView.getAppInstance().getDocumentFile(), list);
    }

    private void filterAndPostComic(final List<Comic> list) {
        final List<Comic> favorite = new LinkedList<>();
        final List<Comic> history = new LinkedList<>();
        mComicManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (Comic comic : list) {
                    Comic temp = mComicManager.load(comic.getSource(), comic.getCid());
                    if (temp == null) {
                        mComicManager.insert(comic);
                        if (comic.getHistory() != null) {
                            history.add(comic);
                        }
                        if (comic.getFavorite() != null) {
                            favorite.add(comic);
                        }
                    } else {
                        if (temp.getFavorite() == null || temp.getHistory() == null) {
                            if (temp.getFavorite() == null && comic.getFavorite() != null) {
                                temp.setFavorite(comic.getFavorite());
                                favorite.add(comic);
                            }
                            if (temp.getHistory() == null && comic.getHistory() != null) {
                                temp.setHistory(comic.getHistory());
                                if (temp.getLast() == null) {
                                    temp.setLast(comic.getLast());
                                    temp.setPage(comic.getPage());
                                    temp.setChapter(comic.getChapter());
                                }
                                history.add(comic);
                            }
                            mComicManager.update(temp);
                        }
                        // TODO 可能要设置其他域
                        comic.setId(temp.getId());
                    }
                }
            }
        });
        postComic(favorite, history);
    }

    private void postComic(List<Comic> favorite, List<Comic> history) {
/*        Collections.sort(favorite, new Comparator<Comic>() {
            @Override
            public int compare(Comic lhs, Comic rhs) {
                return (int) (lhs.getFavorite() - rhs.getFavorite());
            }
        });
        Collections.sort(history, new Comparator<Comic>() {
            @Override
            public int compare(Comic lhs, Comic rhs) {
                return (int) (lhs.getHistory() - rhs.getHistory());
            }
        }); */
        RxBus.getInstance().post(
                new RxEvent(RxEvent.EVENT_COMIC_FAVORITE_RESTORE, convertToMiniComic(favorite)));
        RxBus.getInstance().post(
                new RxEvent(RxEvent.EVENT_COMIC_HISTORY_RESTORE, convertToMiniComic(history)));
    }

    private List<MiniComic> convertToMiniComic(List<Comic> list) {
        List<MiniComic> result = new ArrayList<>(list.size());
        for (Comic comic : list) {
            result.add(new MiniComic(comic));
        }
        return result;
    }

}
