package com.hiroshi.cimoc.ui.view;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public interface MainView extends BaseView {

    void onLastLoadSuccess(int source, String cid, String title, String cover);

    void onLastLoadFail();

    void onLastChange(int source, String cid, String title, String cover);

}
