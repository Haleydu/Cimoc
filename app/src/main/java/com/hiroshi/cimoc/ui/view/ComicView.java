package com.hiroshi.cimoc.ui.view;

import android.support.annotation.ColorRes;

import com.hiroshi.cimoc.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface ComicView extends BaseView {

    void onTagLoadSuccess(List<Tag> list);

    void onTagLoadFail();

    void onTagInsert(Tag tag);

    void onTagDelete(Tag tag);

    void onThemeChange(@ColorRes int primary, @ColorRes int accent);

}
