package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Source;

import java.util.List;

/**
 * Created by Hiroshi on 2016/9/21.
 */

public interface CimocView extends BaseView {

    void onLoadSuccess(List<Source> list);

    void onLoadFail();

}
