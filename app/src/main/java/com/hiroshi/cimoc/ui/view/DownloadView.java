package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.model.Task;

import java.util.ArrayList;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public interface DownloadView extends GridView {

    void onTaskLoadSuccess(ArrayList<Task> list);

    void onTaskLoadFail();

    void onDownloadAdd(MiniComic comic);

    void onDownloadDelete(long id);

    void onDownloadStart();

    void onDownloadStop();

    void onDownloadDeleteSuccess();

    void onDownloadDeleteFail();

}
