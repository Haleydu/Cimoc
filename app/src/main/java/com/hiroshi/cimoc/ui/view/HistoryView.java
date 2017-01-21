package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.component.DialogCaller;
import com.hiroshi.cimoc.model.MiniComic;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface HistoryView extends GridView, DialogCaller {

    void onItemUpdate(MiniComic comic);

    void onHistoryClearSuccess();

    void onHistoryClearFail();

}
