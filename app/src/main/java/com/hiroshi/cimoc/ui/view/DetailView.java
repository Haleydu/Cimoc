package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface DetailView extends BaseView {

    void onComicLoadSuccess(Comic comic);

    void onChapterLoadSuccess(List<Chapter> list);

    void onLastChange(String chapter);

    void onParseError();

    void onTaskAddSuccess(ArrayList<Task> list);

    void onTaskAddFail();

}
