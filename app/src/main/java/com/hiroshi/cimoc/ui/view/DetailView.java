package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.Selectable;
import com.hiroshi.cimoc.model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface DetailView extends BaseView {

    void onDetailLoadSuccess();

    void onComicLoadSuccess(Comic comic);

    void onChapterLoadSuccess(List<Chapter> list);

    void onChapterChange(String chapter);

    void onDownloadLoadSuccess(Set<String> set);

    void onDownloadLoadFail();

    void onTagLoadSuccess(List<Selectable> list);

    void onTagLoadFail();

    void onTagUpdateSuccess();

    void onTagUpdateFail();

    void onTaskAddSuccess(ArrayList<Task> list);

    void onTaskAddFail();

    void onUpdateIndexSuccess();

    void onUpdateIndexFail();

    void onParseError();

    void onNetworkError();

}
