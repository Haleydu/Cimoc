package com.hiroshi.cimoc.ui.view;

import android.support.annotation.ColorRes;

import com.hiroshi.cimoc.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public interface TagView extends BaseView {

    void onTagLoadSuccess(List<Tag> list);

    void onTagLoadFail();

    void onTagDeleteSuccess();

    void onTagDeleteFail();

    void onThemeChange(@ColorRes int primary, @ColorRes int accent);

}
