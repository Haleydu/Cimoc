package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Source;

import java.util.List;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public interface MainView extends BaseView {

    void onLastLoadSuccess(int source, String cid, String title, String cover);

    void onLastLoadFail();

    void onLastChange(int source, String cid, String title, String cover);

    void onSourceLoadSuccess(List<Source> list);

    void onSourceLoadFail();

}
