package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public interface TagView extends CardView {

    void onTagLoadSuccess(List<Tag> list);

    void onTagAddSuccess(Tag tag);

}
