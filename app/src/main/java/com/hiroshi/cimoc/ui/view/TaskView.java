package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Task;

import java.util.List;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public interface TaskView extends BaseView {

    void onTaskLoadSuccess(List<Task> list);

    void onTaskLoadFail();

    void onSortSuccess(List<Task> list);

    void onLoadIndexFail();

    void onTaskAdd(List<Task> list);

    void onTaskParse(long id);

    void onTaskDoing(long id, int max);

    void onTaskProcess(long id, int progress, int max);

    void onTaskFinish(long id);

    void onTaskError(long id);

    void onTaskDeleteSuccess();

    void onTaskDeleteFail();

}
