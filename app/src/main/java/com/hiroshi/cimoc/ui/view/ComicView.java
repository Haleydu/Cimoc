package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Tag;
import com.hiroshi.cimoc.model.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface ComicView extends BaseView, ThemeView, DialogView {

    void onTagLoadSuccess(List<Tag> list);

    void onTagLoadFail();

    void onDownloadStart();

    void onDownloadStop();

    void onTaskLoadSuccess(ArrayList<Task> list);

    void onTaskLoadFail();

}
