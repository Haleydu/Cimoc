package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Comic;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface ResultView extends BaseView {

    void onParseError();

    void onNetworkError();

    void onEmptyResult();

    void onLoadSuccess(Comic comic);

}
