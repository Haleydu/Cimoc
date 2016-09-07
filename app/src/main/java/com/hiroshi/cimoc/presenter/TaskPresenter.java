package com.hiroshi.cimoc.presenter;

import com.hiroshi.cimoc.core.manager.TaskManager;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.rx.RxEvent;
import com.hiroshi.cimoc.ui.view.TaskView;

import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public class TaskPresenter extends BasePresenter<TaskView> {

    private TaskManager mTaskManager;

    public TaskPresenter() {
        mTaskManager = TaskManager.getInstance();
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

    public void load(long key) {
        mTaskManager.list(key)
                .doOnNext(new Action1<List<Task>>() {
                    @Override
                    public void call(List<Task> list) {
                        for (Task task : list) {
                            task.setState(task.getFinish() ? Task.STATE_FINISH : Task.STATE_PAUSE);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Task>>() {
                    @Override
                    public void call(List<Task> list) {
                        mBaseView.onLoadSuccess(list);
                    }
                });
    }

}
