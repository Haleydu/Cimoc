package com.haleydu.cimoc.ui.view;

import com.haleydu.cimoc.model.MiniComic;
import com.haleydu.cimoc.model.Task;

import java.util.ArrayList;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public interface DownloadView extends GridView {

    void onDownloadAdd(MiniComic comic);

    void onDownloadDelete(long id);

    void onDownloadDeleteSuccess(long id);

    void onDownloadStart();

    void onDownloadStop();

    void onTaskLoadSuccess(ArrayList<Task> list);

}
