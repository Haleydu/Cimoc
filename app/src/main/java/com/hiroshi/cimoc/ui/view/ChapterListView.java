package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/11/14.
 */

public interface ChapterListView extends BaseView {

    void onTaskAddSuccess(ArrayList<Task> list);

    void onTaskAddFail();

    void onDownloadLoadSuccess(List<Task> list);

    void onDownloadLoadFail();

    void onUpdateIndexSuccess();

    void onUpdateIndexFail();

}
