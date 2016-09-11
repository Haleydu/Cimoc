package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/9/1.
 */
public interface DownloadView extends BaseView {

    void onLoadSuccess(List<MiniComic> list);

    void onDownloadAdd(MiniComic comic);

    void onDownloadDelete(long id);

}
