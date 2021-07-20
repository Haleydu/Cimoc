package com.haleydu.cimoc.presenter;

import android.util.Pair;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.haleydu.cimoc.core.Download;
import com.haleydu.cimoc.core.Storage;
import com.haleydu.cimoc.manager.ComicManager;
import com.haleydu.cimoc.manager.TaskManager;
import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.model.MiniComic;
import com.haleydu.cimoc.model.Task;
import com.haleydu.cimoc.rx.RxBus;
import com.haleydu.cimoc.rx.RxEvent;
import com.haleydu.cimoc.saf.DocumentFile;
import com.haleydu.cimoc.ui.view.SettingsView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/7/22.
 */
public class SettingsPresenter extends BasePresenter<SettingsView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mTaskManager = TaskManager.getInstance(mBaseView);
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
        // Todo 重写一下
        mCompositeSubscription.add(Download.scan(mBaseView.getAppInstance().getContentResolver(), mBaseView.getAppInstance().getDocumentFile())
                .doOnNext(new Action1<Pair<Comic, List<Task>>>() {
                    @Override
                    public void call(Pair<Comic, List<Task>> pair) {
                        Comic comic = mComicManager.load(pair.first.getSource(), pair.first.getCid());
                        if (comic == null) {
                            mComicManager.insert(pair.first);
                            updateKey(pair.first.getId(), pair.second);
                            mTaskManager.insertInTx(pair.second);
                            comic = pair.first;
                        } else {
                            comic.setDownload(System.currentTimeMillis());
                            mComicManager.update(comic);
                            updateKey(comic.getId(), pair.second);
                            mTaskManager.insertIfNotExist(pair.second);
                        }
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_INSERT, new MiniComic(comic)));
                        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DIALOG_PROGRESS, comic.getTitle()));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Pair<Comic, List<Task>>>() {
                    @Override
                    public void call(Pair<Comic, List<Task>> pair) {
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

}
