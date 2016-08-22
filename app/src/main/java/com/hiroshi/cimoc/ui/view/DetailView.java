package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface DetailView extends BaseView {

    void showLayout();

    void onLoadSuccess(Comic comic, List<Chapter> list);

    void onChapterChange(String chapter);

    void onParseError();

    void onNetworkError();

}
