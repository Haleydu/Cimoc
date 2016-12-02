package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Backup;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.RxObject;
import com.hiroshi.cimoc.ui.view.BackupView;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/10/19.
 */

public class BackupPresenter extends BasePresenter<BackupView> {

    private ComicManager mComicManager;
    private TagManager mTagManager;
    private List<Tag> mTagList;

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
                        mTagList = list;
                        String[] array = new String[list.size()];
                        for (int i = 0; i != array.length; ++i) {
                            array[i] = list.get(i).getTitle();
                        }
                        mBaseView.onTagLoadSuccess(array);
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

    public void saveTag(int index) {
        final Tag tag = mTagList.get(index);
        mCompositeSubscription.add(mTagManager.listByTag(tag.getId())
                .flatMap(new Func1<List<TagRef>, Observable<List<Comic>>>() {
                    @Override
                    public Observable<List<Comic>> call(final List<TagRef> list) {
                        return mComicManager.callInRx(new Callable<List<Comic>>() {
                            @Override
                            public List<Comic> call() throws Exception {
                                List<Comic> result = new LinkedList<>();
                                for (TagRef ref : list) {
                                    result.add(mComicManager.load(ref.getCid()));
                                }
                                return result;
                            }
                        });
                    }
                })
                .flatMap(new Func1<List<Comic>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(List<Comic> list) {
                        return Backup.saveTag(tag, list);
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

    public void restoreFavorite(final String filename) {
        mCompositeSubscription.add(Backup.restoreFavorite(filename)
                .flatMap(new Func1<List<Comic>, Observable<List<MiniComic>>>() {
                    @Override
                    public Observable<List<MiniComic>> call(final List<Comic> list) {
                        return mComicManager.callInRx(new Callable<List<MiniComic>>() {
                            @Override
                            public List<MiniComic> call() throws Exception {
                                long favorite = System.currentTimeMillis() + list.size() * 10;
                                List<MiniComic> result = new LinkedList<>();
                                for (Comic comic : list) {
                                    Comic temp = mComicManager.load(comic.getSource(), comic.getCid());
                                    if (temp == null) {
                                        comic.setFavorite(favorite);
                                        mComicManager.insert(comic);
                                        result.add(new MiniComic(comic));
                                    } else if (temp.getFavorite() == null) {
                                        temp.setFavorite(favorite);
                                        mComicManager.update(temp);
                                        result.add(new MiniComic(temp));
                                    }
                                    favorite -= 20;
                                }
                                return result;
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

    @SuppressWarnings("unchecked")
    public void restoreTag(final String filename) {
        mCompositeSubscription.add(Backup.restoreTag(filename)
                .flatMap(new Func1<RxObject, Observable<RxObject>>() {
                    @Override
                    public Observable<RxObject> call(final RxObject object) {
                        return mComicManager.callInRx(new Callable<RxObject>() {
                            @Override
                            public RxObject call() throws Exception {
                                boolean insert = false;
                                String title = (String) object.getData();
                                Tag tag = mTagManager.load(title);
                                if (tag == null) {
                                    insert = true;
                                    tag = new Tag(null, title);
                                    long tid = mTagManager.insert(tag);
                                    tag.setId(tid);
                                }

                                List<Comic> list = (List<Comic>) object.getData(1);
                                long favorite = System.currentTimeMillis() + list.size() * 10;
                                List<MiniComic> result = new LinkedList<>();
                                for (Comic comic : list) {
                                    Comic temp = mComicManager.load(comic.getSource(), comic.getCid());
                                    if (temp == null) {
                                        temp = comic;
                                        comic.setFavorite(favorite);
                                        mComicManager.insert(comic);
                                    } else if (temp.getFavorite() == null) {
                                        temp.setFavorite(favorite);
                                        mComicManager.update(temp);
                                        result.add(new MiniComic(temp));
                                    }

                                    TagRef ref = mTagManager.load(tag.getId(), temp.getId());
                                    if (ref == null) {
                                        mTagManager.insert(new TagRef(null, tag.getId(), temp.getId()));
                                    }
                                    favorite -= 20;
                                }

                                return new RxObject(insert ? tag : null, result);
                            }
                        });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<RxObject>() {
                    @Override
                    public void call(RxObject object) {
                        Object tag = object.getData();
                        if (tag != null) {
                            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TAG_INSERT, tag));
                        }
                        List<Comic> list = (List<Comic>) object.getData(1);
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

}
