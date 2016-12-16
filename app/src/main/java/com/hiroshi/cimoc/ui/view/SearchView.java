package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Source;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface SearchView extends BaseView, DialogView {

    void onSourceLoadSuccess(List<Source> list);

    void onSourceLoadFail();

    void onAutoCompleteLoadSuccess(List<String> list);

}
