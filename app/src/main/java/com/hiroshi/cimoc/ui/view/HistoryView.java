package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface HistoryView extends BaseView {

    void onItemClear();

    void onItemUpdate(MiniComic comic);

    void onLoadSuccess(List<MiniComic> list);

    void onSourceRemove(int source);

}
