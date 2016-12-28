package com.hiroshi.cimoc.presenter;

import android.support.annotation.ColorRes;
import android.support.annotation.StyleRes;
import android.support.v4.provider.DocumentFile;
import android.support.v4.util.LongSparseArray;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.core.Storage;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.SettingsView;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsPresenter extends BasePresenter<SettingsView> {

    ComicManager mComicManager;
    TaskManager mTaskManager;

    public SettingsPresenter() {
        mComicManager = ComicManager.getInstance();
        mTaskManager = TaskManager.getInstance();
    }

    public void clearCache() {
        Fresco.getImagePipeline().clearDiskCaches();
    }

    public void moveFiles(DocumentFile dst) {
        mCompositeSubscription.add(Storage.moveRootDir(dst)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String msg) {
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, msg));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onExecuteFail();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mBaseView.onFileMoveSuccess();
                    }
                }));
    }

    private void updateKey(long key, List<Task> list) {
        for (Task task : list) {
            task.setKey(key);
        }
    }

    public void scanTask() {
        mCompositeSubscription.add(Download.scan()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair<Comic, List<Task>>>() {
                    @Override
                    public void call(Pair<Comic, List<Task>> pair) {
                        Comic comic = mComicManager.load(pair.first.getSource(), pair.first.getCid());
                        if (comic == null) {
                            mComicManager.insert(pair.first);
                            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, pair.first.getTitle()));
                            updateKey(pair.first.getId(), pair.second);
                            mTaskManager.insertInTx(pair.second);
                        } else {
                            updateKey(comic.getId(), pair.second);
                            mTaskManager.insertIfNotExist(pair.second);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        mBaseView.onExecuteFail();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mBaseView.onDownloadScanSuccess();
                    }
                }));
    }

    public void deleteTask() {
        // 停止检查更新
        mCompositeSubscription.add(Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("正在读取漫画");
                LongSparseArray<Comic> array = buildComicMap();
                subscriber.onNext("正在搜索无效任务");
                List<Task> list = findInvalid(array);
                subscriber.onNext("正在删除无效任务");
                deleteInvalid(array, list);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String msg) {
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, msg));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onExecuteFail();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mBaseView.onDownloadDeleteSuccess();
                    }
                }));
    }

    private List<Task> findInvalid(LongSparseArray<Comic> array) {
        List<Task> list = new LinkedList<>();
        Set<Long> set = new HashSet<>();
        for (Task task : mTaskManager.listComplete()) {
            Comic comic = array.get(task.getKey());
            if (comic == null || Download.getChapterDir(comic, new Chapter(task.getTitle(), task.getPath())) == null) {
                list.add(task);
            } else {
                set.add(task.getKey());
            }
        }
        for (Long id : set) {
            array.remove(id);
        }
        return list;
    }

    private void deleteInvalid(final LongSparseArray<Comic> array, final List<Task> list) {
        mComicManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i != array.size(); ++i) {
                    Comic comic = array.valueAt(i);
                    comic.setDownload(null);
                    mComicManager.updateOrDelete(comic);
                }
                mTaskManager.deleteInTx(list);
            }
        });
    }

    private LongSparseArray<Comic> buildComicMap() {
        LongSparseArray<Comic> array = new LongSparseArray<>();
        for (Comic comic : mComicManager.listDownload()) {
            array.put(comic.getId(), comic);
        }
        return array;
    }

    public void changeTheme(@StyleRes int theme, @ColorRes int primary, @ColorRes int accent) {
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_THEME_CHANGE, theme, primary, accent));
    }

}
