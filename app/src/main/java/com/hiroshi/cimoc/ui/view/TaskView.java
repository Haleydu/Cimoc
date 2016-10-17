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

    void onChapterChange(String last);

    void onLoadIndexFail();

    void onTaskAdd(List<Task> list);

    void onTaskParse(long id);

    void onTaskProcess(long id, int progress, int max);

    void onTaskError(long id);

    void onTaskDeleteSuccess();

    void onTaskDeleteFail();

}
