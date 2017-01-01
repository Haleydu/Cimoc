package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.ToAnotherList;
import com.hiroshi.cimoc.ui.view.DownloadView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public class DownloadPresenter extends BasePresenter<DownloadView> {

    private ComicManager mComicManager;
    private TaskManager mTaskManager;

    public DownloadPresenter() {
        mComicManager = ComicManager.getInstance();
        mTaskManager = TaskManager.getInstance();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_TASK_INSERT, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onDownloadAdd((MiniComic) rxEvent.getData());
            }
        });
        addSubscription(RxEvent.EVENT_DOWNLOAD_REMOVE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                mBaseView.onDownloadDelete((long) rxEvent.getData());
            }
        });
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

    public void deleteComic(final long id) {
        mCompositeSubscription.add(Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                final Comic comic = mComicManager.load(id);
                mComicManager.runInTx(new Runnable() {
                    @Override
                    public void run() {
                        mTaskManager.deleteInTx(id);
                        comic.setDownload(null);
                        mComicManager.updateOrDelete(comic);
                    }
                });
                Download.delete(comic);
                subscriber.onNext(null);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void v) {
                        mBaseView.onDownloadDeleteSuccess(id);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onDownloadDeleteFail();
                    }
                }));
    }

    public void loadComic() {
        mCompositeSubscription.add(mComicManager.listDownloadInRx()
                .compose(new ToAnotherList<>(new Func1<Comic, MiniComic>() {
                    @Override
                    public MiniComic call(Comic comic) {
                        return new MiniComic(comic);
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<MiniComic>>() {
                    @Override
                    public void call(List<MiniComic> list) {
                        mBaseView.onComicLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onComicLoadFail();
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
