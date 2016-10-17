package com.hiroshi.cimoc.presenter;

import android.support.annotation.ColorRes;
import android.support.annotation.StyleRes;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.core.Backup;
import com.hiroshi.cimoc.core.Storage;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.SettingsView;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsPresenter extends BasePresenter<SettingsView> {

    private ComicManager mComicManager;

    public SettingsPresenter() {
        mComicManager = ComicManager.getInstance();
    }

    public void clearCache() {
        Fresco.getImagePipeline().clearDiskCaches();
    }

    public void backup() {
        mCompositeSubscription.add(mComicManager.listFavorite()
                .flatMap(new Func1<List<Comic>, Observable<Integer>>() {
                    @Override
                    public Observable<Integer> call(List<Comic> list) {
                        return Backup.save(list);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer integer) {
                        mBaseView.onBackupSuccess(integer);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onBackupFail();
                    }
                }));
    }

    public void loadFiles() {
        mCompositeSubscription.add(Backup.load()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String[]>() {
                    @Override
                    public void call(String[] files) {
                        mBaseView.onFilesLoadSuccess(files);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onFilesLoadFail();
                    }
                }));
    }

    public void moveFiles(final String path) {
        mCompositeSubscription.add(Storage.moveFolder(path)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        mBaseView.onFileMoveSuccess(path);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onFileMoveFail();
                    }
                }));
    }

    public void restore(String name) {
        mCompositeSubscription.add(Backup.restore(name)
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
                                        long id = mComicManager.insert(comic);
                                        comic.setId(id);
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
                        mBaseView.onRestoreSuccess(list.size());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onRestoreFail();
                    }
                }));
    }

    public void changeTheme(@StyleRes int theme, @ColorRes int primary, @ColorRes int accent) {
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_THEME_CHANGE, theme, primary, accent));
    }

}
