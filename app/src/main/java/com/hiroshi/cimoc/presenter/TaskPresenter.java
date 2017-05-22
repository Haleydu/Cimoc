package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.manager.ComicManager;
import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.manager.TaskManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.rx.ToAnotherList;
import com.hiroshi.cimoc.ui.view.TaskView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public class TaskPresenter extends BasePresenter<TaskView> {

    private TaskManager mTaskManager;
    private ComicManager mComicManager;
    private SourceManager mSourceManager;
    private Comic mComic;

    @Override
    protected void onViewAttach() {
        mTaskManager = TaskManager.getInstance(mBaseView);
        mComicManager = ComicManager.getInstance(mBaseView);
        mSourceManager = SourceManager.getInstance(mBaseView);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.EVENT_TASK_STATE_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                long id = (long) rxEvent.getData(1);
                switch ((int) rxEvent.getData()) {
                    case Task.STATE_PARSE:
                        mBaseView.onTaskParse(id);
                        break;
                    case Task.STATE_ERROR:
                        mBaseView.onTaskError(id);
                        break;
                    case Task.STATE_PAUSE:
                        mBaseView.onTaskPause(id);
                        break;
                }
            }
        });
        addSubscription(RxEvent.EVENT_TASK_PROCESS, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                long id = (long) rxEvent.getData();
                mBaseView.onTaskProcess(id, (int) rxEvent.getData(1), (int) rxEvent.getData(2));
            }
        });
        addSubscription(RxEvent.EVENT_TASK_INSERT, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                List<Task> list = (List<Task>) rxEvent.getData(1);
                Task task = list.get(0);
                if (task.getKey() == mComic.getId()) {
                    mBaseView.onTaskAdd(list);
                }
            }
        });
        addSubscription(RxEvent.EVENT_COMIC_UPDATE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                if (mComic.getId() != null && mComic.getId() == (long) rxEvent.getData()) {
                    Comic comic = mComicManager.load(mComic.getId());
                    mComic.setPage(comic.getPage());
                    mComic.setLast(comic.getLast());
                    mBaseView.onLastChange(mComic.getLast());
                }
            }
        });
    }

    public Comic getComic() {
        return mComic;
    }

    private void updateTaskList(List<Task> list) {
        for (Task task : list) {
            int state = task.isFinish() ? Task.STATE_FINISH : Task.STATE_PAUSE;
            task.setCid(mComic.getCid());
            task.setSource(mComic.getSource());
            task.setState(state);
        }
    }

    public void load(long id, final boolean asc) {
        mComic = mComicManager.load(id);
        mCompositeSubscription.add(mTaskManager.listInRx(id)
                .doOnNext(new Action1<List<Task>>() {
                    @Override
                    public void call(List<Task> list) {
                        updateTaskList(list);
                        if (!mComic.getLocal()) {
                            final List<String> sList = Download.getComicIndex(mBaseView.getAppInstance().getContentResolver(),
                                    mBaseView.getAppInstance().getDocumentFile(), mComic, mSourceManager.getParser(mComic.getSource()).getTitle());
                            if (sList != null) {
                                Collections.sort(list, new Comparator<Task>() {
                                    @Override
                                    public int compare(Task lhs, Task rhs) {
                                        return asc ? sList.indexOf(rhs.getPath()) - sList.indexOf(lhs.getPath()) :
                                                sList.indexOf(lhs.getPath()) - sList.indexOf(rhs.getPath());
                                    }
                                });
                            }
                        } else {
                            Collections.sort(list, new Comparator<Task>() {
                                @Override
                                public int compare(Task lhs, Task rhs) {
                                    return asc ? lhs.getTitle().compareTo(rhs.getTitle()) :
                                            rhs.getTitle().compareTo(lhs.getTitle());
                                }
                            });
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Task>>() {
                    @Override
                    public void call(List<Task> list) {
                        mBaseView.onTaskLoadSuccess(list, mComic.getLocal());
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTaskLoadFail();
                    }
                }));
    }

    public void deleteTask(List<Chapter> list, final boolean isEmpty) {
        final long id = mComic.getId();
        mCompositeSubscription.add(Observable.just(list)
                .subscribeOn(Schedulers.io())
                .doOnNext(new Action1<List<Chapter>>() {
                    @Override
                    public void call(List<Chapter> list) {
                        deleteFromDatabase(list, isEmpty);
                        if (!mComic.getLocal()) {
                            if (isEmpty) {
                                Download.delete(mBaseView.getAppInstance().getDocumentFile(), mComic,
                                        mSourceManager.getParser(mComic.getSource()).getTitle());
                            } else {
                                Download.delete(mBaseView.getAppInstance().getDocumentFile(), mComic,
                                        list, mSourceManager.getParser(mComic.getSource()).getTitle());
                            }
                        }
                    }
                })
                .compose(new ToAnotherList<>(new Func1<Chapter, Long>() {
                    @Override
                    public Long call(Chapter chapter) {
                        return chapter.getTid();
                    }
                }))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Long>>() {
                    @Override
                    public void call(List<Long> list) {
                        if (isEmpty) {
                            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_REMOVE, id));
                        }
                        mBaseView.onTaskDeleteSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTaskDeleteFail();
                    }
                }));
    }

    private void deleteFromDatabase(final List<Chapter> list, final boolean isEmpty) {
        mComicManager.runInTx(new Runnable() {
            @Override
            public void run() {
                for (Chapter chapter : list) {
                    mTaskManager.delete(chapter.getTid());
                }
                if (isEmpty) {
                    mComic.setDownload(null);
                    mComicManager.updateOrDelete(mComic);
                    Download.delete(mBaseView.getAppInstance().getDocumentFile(), mComic,
                            mSourceManager.getParser(mComic.getSource()).getTitle());
                }
            }
        });
    }

    public long updateLast(String path) {
        if (mComic.getFavorite() != null) {
            mComic.setFavorite(System.currentTimeMillis());
        }
        mComic.setHistory(System.currentTimeMillis());
        if (!path.equals(mComic.getLast())) {
            mComic.setLast(path);
            mComic.setPage(1);
        }
        mComicManager.update(mComic);
        RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_COMIC_READ, new MiniComic(mComic), false));
        return mComic.getId();
    }

}