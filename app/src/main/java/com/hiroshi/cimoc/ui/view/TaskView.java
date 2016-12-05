package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Task;

import java.util.List;

/**
 * Created by Hiroshi on 2016/9/7.
 */
public interface TaskView extends BaseView, DialogView {

    void onTaskLoadSuccess(List<Task> list);

    void onTaskLoadFail();

    void onChapterChange(String last);

    void onTaskAdd(List<Task> list);

    void onTaskParse(long id);

    void onTaskProcess(long id, int progress, int max);

    void onTaskError(long id);

    void onTaskDeleteSuccess(List<Task> list);

    void onTaskDeleteFail();

}
