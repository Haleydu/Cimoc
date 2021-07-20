package com.haleydu.cimoc.ui.view;

import com.haleydu.cimoc.component.DialogCaller;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public interface MainView extends BaseView, DialogCaller {

    void onLastLoadSuccess(long id, int source, String cid, String title, String cover);

    void onLastLoadFail();

    void onLastChange(long id, int source, String cid, String title, String cover);

    void onUpdateReady();

    void onUpdateReady(String versionName, String content, String mUrl, int versionCode, String md5);
}
