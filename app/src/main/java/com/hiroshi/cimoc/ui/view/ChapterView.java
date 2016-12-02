package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Task;

import java.util.ArrayList;

/**
 * Created by Hiroshi on 2016/11/14.
 */

public interface ChapterView extends BaseView {

    void onTaskAddSuccess(ArrayList<Task> list);

    void onTaskAddFail();

}
