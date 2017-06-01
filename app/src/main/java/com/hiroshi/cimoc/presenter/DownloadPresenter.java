package com.hiroshi.cimoc.presenter;

import android.support.v4.util.LongSparseArray;

import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.manager.ComicManager;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.manager.TaskManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.ToAnotherList;
import com.hiroshi.cimoc.ui.view.DownloadView;
import com.hiroshi.cimoc.utils.ComicUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;
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
    private SourceManager mSourceManager;

    @Override
    protected void onViewAttach() {
        mComicManager = ComicManager.getInstance(mBaseView);
        mTaskManager = TaskManager.getInstance(mBaseView);
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    @SuppressWarnings("unchecked")
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
        addSubscription(RxEvent.EVENT_DOWNLOAD_CLEAR, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                for (long id : (List<Long>) rxEvent.getData()) {
                    mBaseView.onDownloadDelete(id);
                }
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

    public void deleteComic(long id) {
        mCompositeSubscription.add(Observable.just(id)
                .doOnNext(new Action1<Long>() {
                    @Override
                    public void call(final Long id) {
                        Comic comic = mComicManager.callInTx(new Callable<Comic>() {
                            @Override
                            public Comic call() throws Exception {
                                Comic comic = mComicManager.load(id);
                                mTaskManager.deleteByComicId(id);
                                comic.setDownload(null);
                                mComicManager.updateOrDelete(comic);
                                return comic;
                            }
                        });
                        Download.delete(mBaseView.getAppInstance().getDocumentFile(), comic, mSourceManager.getParser(comic.getSource()).getTitle());
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long id) {
                        mBaseView.onDownloadDeleteSuccess(id);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onExecuteFail();
                    }
                }));
    }

    public Comic load(long id) {
        return mComicManager.load(id);
    }

    public void load() {
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
                .doOnNext(new Action1<List<Task>>() {
                    @Override
                    public void call(List<Task> list) {
                        LongSparseArray<Comic> array = ComicUtils.buildComicMap(mComicManager.listDownload());
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
                        mBaseView.onExecuteFail();
                    }
                }));
    }

}
