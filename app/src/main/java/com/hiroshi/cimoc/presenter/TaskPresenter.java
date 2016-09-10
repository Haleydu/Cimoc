package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.Index;
import com.hiroshi.cimoc.core.manager.ComicManager;
import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.TaskView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public class TaskPresenter extends BasePresenter<TaskView> {

    private TaskManager mTaskManager;
    private ComicManager mComicManager;

    public TaskPresenter() {
        mTaskManager = TaskManager.getInstance();
        mComicManager = ComicManager.getInstance();
    }

    @Override
    protected void initSubscription() {
        addSubscription(RxEvent.DOWNLOAD_STATE_CHANGE, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                long id = (long) rxEvent.getData(1);
                switch ((int) rxEvent.getData()) {
                    case Task.STATE_PARSE:
                        mBaseView.onTaskParse(id);
                        break;
                    case Task.STATE_DOING:
                        mBaseView.onTaskDoing(id, (int) rxEvent.getData(2));
                        break;
                    case Task.STATE_FINISH:
                        mBaseView.onTaskFinish(id);
                        break;
                    case Task.STATE_ERROR:
                        mBaseView.onTaskError(id);
                        break;
                }
            }
        });
        addSubscription(RxEvent.DOWNLOAD_PROCESS, new Action1<RxEvent>() {
            @Override
            public void call(RxEvent rxEvent) {
                long id = (long) rxEvent.getData();
                mBaseView.onTaskProcess(id, (int) rxEvent.getData(1), (int) rxEvent.getData(2));
            }
        });
    }

    public void loadTask(long key) {
        mTaskManager.list(key)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Task>>() {
                    @Override
                    public void call(List<Task> list) {
                        for (Task task : list) {
                            int state = task.getMax() != 0 && task.getProgress() == task.getMax() ?
                                    Task.STATE_FINISH : Task.STATE_PAUSE;
                            task.setState(state);
                        }
                        mBaseView.onLoadSuccess(list);
                    }
                });
    }

    public void sortTask(final List<Task> list, int source, String comic) {
        Index.get(source, comic)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(final List<String> paths) {
                        Collections.sort(list, new Comparator<Task>() {
                            @Override
                            public int compare(Task lhs, Task rhs) {
                                return paths.indexOf(lhs.getPath()) - paths.indexOf(rhs.getPath());
                            }
                        });
                        mBaseView.onSortSuccess(list);
                    }
                });
    }

    public void deleteTask(Task task, boolean isEmpty) {
        mTaskManager.delete(task);
        if (isEmpty) {
            Comic comic = mComicManager.load(task.getId());
            if (comic.getFavorite() == null && comic.getHistory() == null) {
                mComicManager.delete(comic);
            } else {
                comic.setDownload(null);
                mComicManager.update(comic);
            }
        }
    }

}
