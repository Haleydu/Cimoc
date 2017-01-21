package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.component.DialogCaller;

/**
 * Created by Hiroshi on 2017/1/18.
 */

public interface SourceDetailView extends BaseView, DialogCaller {

    void onSourceLoadSuccess(int type, String title, long count, String server);

}
