package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface ComicView extends BaseView, ThemeView, DialogView {

    void onTagLoadSuccess(List<Tag> list);

    void onTagLoadFail();

}
