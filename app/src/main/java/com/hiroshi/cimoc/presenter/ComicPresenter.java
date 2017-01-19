package com.hiroshi.cimoc.presenter;

import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TagManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.ComicView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public class ComicPresenter extends BasePresenter<ComicView> {

    private TagManager mTagManager;
    private TaskManager mTaskManager;
    private ComicManager mComicManager;

    @Override
    protected void onViewAttach() {
        mTagManager = TagManager.getInstance(mBaseView);
        mTaskManager = TaskManager.getInstance(mBaseView);
        mComicManager = ComicManager.getInstance(mBaseView);
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_DOWNLOAD_START, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onDownloadStart();
            }
        });
        addSubscription(RxEvent.EVENT_DOWNLOAD_STOP, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onDownloadStop();
            }
        });
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
                        mBaseView.onTagLoadFail();
                    }
                }));
    }

    public void loadTask() {
        mCompositeSubscription.add(mTaskManager.listInRx()
                .flatMap(new Func1<List<Task>, Observable<Task>>() {
                    @Override
                    public Observable<Task> call(List<Task> list) {
                        return Observable.from(list);
                    }
                })
                .filter(new Func1<Task, Boolean>() {
                    @Override
                    public Boolean call(Task task) {
                        return !task.isFinish();
                    }
                })
                .toList()
                .doOnNext(new Action1<List<Task>>() {
                    @Override
                    public void call(List<Task> list) {
                        LongSparseArray<Comic> array = new LongSparseArray<>();
                        for (Comic comic : mComicManager.listDownload()) {
                            array.put(comic.getId(), comic);
                        }
                        for (Task task : list) {
                            Comic comic = array.get(task.getKey());
                            if (comic != null) {
                                task.setSource(comic.getSource());
                                task.setCid(comic.getCid());
                            }
                            task.setState(Task.STATE_WAIT);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Task>>() {
                    @Override
                    public void call(List<Task> list) {
                        mBaseView.onTaskLoadSuccess(new ArrayList<>(list));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTaskLoadFail();
                    }
                }));
    }

}
