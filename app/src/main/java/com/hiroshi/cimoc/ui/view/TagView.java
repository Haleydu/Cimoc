package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public interface TagView extends BaseView, ThemeView, DialogView {

    void onTagLoadSuccess(List<Tag> list);

    void onTagLoadFail();

    void onTagDeleteSuccess(Tag tag);

    void onTagDeleteFail();

    void onTagRestore(Tag tag);

}
