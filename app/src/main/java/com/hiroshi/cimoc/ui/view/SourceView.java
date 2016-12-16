package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Source;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface SourceView extends BaseView, ThemeView, DialogView {

    void onSourceLoadSuccess(List<Source> list);

    void onSourceLoadFail();

}
