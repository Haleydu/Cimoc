package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Download;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxBus;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.TaskView;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
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
    private Comic mComic;

    public TaskPresenter() {
        mTaskManager = TaskManager.getInstance();
        mComicManager = ComicManager.getInstance();
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
        addSubscription(RxEvent.EVENT_COMIC_CHAPTER_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                String path = (String) rxEvent.getData();
                mComic.setLast(path);
                mBaseView.onLastChange(path);
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
                        final List<String> sList = Download.getComicIndex(mComic);
                        if (sList != null) {
                            Collections.sort(list, new Comparator<Task>() {
                                @Override
                                public int compare(Task lhs, Task rhs) {
                                    if (asc) {
                                        return sList.indexOf(rhs.getPath()) - sList.indexOf(lhs.getPath());
                                    } else {
                                        return sList.indexOf(lhs.getPath()) - sList.indexOf(rhs.getPath());
                                    }
                                }
                            });
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Task>>() {
                    @Override
                    public void call(List<Task> list) {
                        mBaseView.onTaskLoadSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                        mBaseView.onTaskLoadFail();
                    }
                }));
    }

    private List<Chapter> buildChapterList(List<Task> list) {
        List<Chapter> result = new LinkedList<>();
        for (Task task : list) {
            result.add(new Chapter(task.getTitle(), task.getPath()));
        }
        return result;
    }

    public void deleteTask(final List<Task> list, final boolean isEmpty) {
        mCompositeSubscription.add(Observable.from(list)
                .observeOn(Schedulers.io())
                .map(new Func1<Task, String>() {
                    @Override
                    public String call(Task task) {
                        return task.getTitle();
                    }
                })
                .toList()
                .doOnNext(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> strings) {
                        Download.delete(mComic, buildChapterList(list));
                        mTaskManager.deleteInTx(list);
                        if (isEmpty) {
                            long id = mComic.getId();
                            mComic.setDownload(null);
                            mComicManager.updateOrDelete(mComic);
                            Download.delete(mComic);
                            RxBus.getInstance().post(new RxEvent(RxEvent.EVENT_DOWNLOAD_REMOVE, id));
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> strings) {
                        mBaseView.onTaskDeleteSuccess(list);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mBaseView.onTaskDeleteFail();
                    }
                }));
    }

    public long updateLast(String path) {
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