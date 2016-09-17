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

    void onDetailLoadSuccess();

    void onComicLoad(Comic comic);

    void onChapterLoad(List<Chapter> list);

    void onChapterChange(String chapter);

    void onDownloadLoadSuccess(boolean[] download, boolean[] complete);

    void onDownloadLoadFail();

    void onTaskAddSuccess(ArrayList<Task> list);

    void onTaskAddFail();

    void onUpdateIndexSuccess();

    void onUpdateIndexFail();

    void onParseError();

    void onNetworkError();

}
