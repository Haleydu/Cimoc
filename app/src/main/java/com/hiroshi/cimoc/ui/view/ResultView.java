package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Comic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface ResultView extends BaseView {

    void onSearchError();

    void onResultEmpty();

    void onSearchSuccess(Comic comic);

    void onRecentLoadSuccess(List<Comic> list);

    void onRecentLoadFail();

}
