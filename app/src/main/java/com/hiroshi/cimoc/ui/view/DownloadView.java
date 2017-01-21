package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.component.DialogCaller;
import com.hiroshi.cimoc.model.MiniComic;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public interface DownloadView extends GridView, DialogCaller {

    void onDownloadAdd(MiniComic comic);

    void onDownloadDelete(long id);

    void onDownloadDeleteSuccess(long id);

    void onDownloadDeleteFail();

}
