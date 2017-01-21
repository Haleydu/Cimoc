package com.hiroshi.cimoc.presenter;

import android.support.v4.provider.DocumentFile;
import android.support.v4.util.LongSparseArray;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.core.Storage;
import com.hiroshi.cimoc.manager.ComicManager;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.manager.TaskManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
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

    private ComicManager mComicManager;
    private TaskManager mTaskManager;
    private SourceManager mSourceManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mTaskManager = TaskManager.getInstance(mBaseView);
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    public void clearCache() {
        Fresco.getImagePipeline().clearDiskCaches();
    }

    public void moveFiles(DocumentFile dst) {
        mCompositeSubscription.add(Storage.moveRootDir(mBaseView.getAppInstance().getContentResolver(),
                mBaseView.getAppInstance().getDocumentFile(), dst)
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
        // Todo
        mCompositeSubscription.add(Download.scan(mBaseView.getAppInstance().getContentResolver(), mBaseView.getAppInstance().getDocumentFile())
                .doOnNext(new Action1<Pair<Comic, List<Task>>>() {
                    @Override
                    public void call(Pair<Comic, List<Task>> pair) {
                        Comic comic = mComicManager.load(pair.first.getSource(), pair.first.getCid());
                        if (comic == null) {
                            mComicManager.insert(pair.first);
                            updateKey(pair.first.getId(), pair.second);
                            mTaskManager.insertInTx(pair.second);
                            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_INSERT, new MiniComic(pair.first)));
                            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, pair.first.getTitle()));
                        } else {
                            comic.setDownload(System.currentTimeMillis());
                            mComicManager.update(comic);
                            updateKey(comic.getId(), pair.second);
                            mTaskManager.insertIfNotExist(pair.second);
                            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_INSERT, new MiniComic(comic)));
                            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, comic.getTitle()));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair<Comic, List<Task>>>() {
                    @Override
                    public void call(Pair<Comic, List<Task>> pair) {}
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onExecuteFail();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        mBaseView.onExecuteSuccess();
                    }
                }));
    }

    public void deleteTask() {
        mCompositeSubscription.add(Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("正在读取漫画");
                LongSparseArray<Comic> array = buildComicMap();
                subscriber.onNext("正在搜索无效任务");
                Pair<List<Comic>, List<Task>> pair = findInvalid(array);
                subscriber.onNext("正在删除无效任务");
                deleteInvalid(pair.first, pair.second);
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
                        mBaseView.onExecuteSuccess();
                    }
                }));
    }

    private Pair<List<Comic>, List<Task>> findInvalid(LongSparseArray<Comic> array) {
        List<Task> tList = new LinkedList<>();
        List<Comic> cList = new LinkedList<>();
        Set<Long> set = new HashSet<>();
        for (Task task : mTaskManager.listValid()) {
            Comic comic = array.get(task.getKey());
            if (comic == null || Download.getChapterDir(mBaseView.getAppInstance().getDocumentFile(), comic,
                    new Chapter(task.getTitle(), task.getPath()), mSourceManager.getParser(comic.getSource()).getTitle()) == null) {
                tList.add(task);
            } else {
                set.add(task.getKey());
            }
        }
        for (int i = 0; i != array.size(); ++i) {
            if (!set.contains(array.keyAt(i))) {
                cList.add(array.valueAt(i));
            }
        }
        return Pair.create(cList, tList);
    }

    private void deleteInvalid(final List<Comic> cList, final List<Task> tList) {
        List<Long> list = new LinkedList<>();
        for (Comic comic : cList) {
            list.add(comic.getId());
        }
        mComicManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (Comic comic : cList) {
                    comic.setDownload(null);
                    mComicManager.updateOrDelete(comic);
                }
                mTaskManager.deleteInTx(tList);
            }
        });
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_CLEAR, list));
    }

    private LongSparseArray<Comic> buildComicMap() {
        LongSparseArray<Comic> array = new LongSparseArray<>();
        for (Comic comic : mComicManager.listDownload()) {
            array.put(comic.getId(), comic);
        }
        return array;
    }

}
