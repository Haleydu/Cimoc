package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.MiniComic;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public interface DownloadView extends GridView, DialogView {

    void onDownloadAdd(MiniComic comic);

    void onDownloadDelete(long id);

    void onDownloadDeleteSuccess(long id);

    void onDownloadDeleteFail();

}
