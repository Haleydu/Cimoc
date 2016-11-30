package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.global.SharedComic;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.ChapterListView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/11/14.
 */

public class ChapterListPresenter extends BasePresenter<ChapterListView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;
    private SharedComic mSharedComic;

    public ChapterListPresenter() {
        mComicManager = ComicManager.getInstance();
        mTaskManager = TaskManager.getInstance();
        mSharedComic = SharedComic.getInstance();
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

    public void addTask(final List<Chapter> list) {
        mCompositeSubscription.add(mComicManager.callInRx(new Callable<ArrayList<Task>>() {
            @Override
            public ArrayList<Task> call() throws Exception {
                Long key = mSharedComic.id();
                mSharedComic.get().setDownload(System.currentTimeMillis());
                mSharedComic.updateOrInsert();
                ArrayList<Task> taskList = new ArrayList<>(list.size());
                for (Chapter chapter : list) {
                    Task task = new Task(null, key, chapter.getPath(), chapter.getTitle(), 0, 0);
                    long id = mTaskManager.insert(task);
                    task.setId(id);
                    task.setInfo(mSharedComic.source(), mSharedComic.cid(), mSharedComic.title());
                    task.setState(Task.STATE_WAIT);
                    taskList.add(task);
                }
                RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_TASK_INSERT, mSharedComic.minify(), taskList));
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

    public void updateIndex(List<Chapter> list) {
        mCompositeSubscription.add(Download.updateComicIndex(list, mSharedComic.get())
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

}
