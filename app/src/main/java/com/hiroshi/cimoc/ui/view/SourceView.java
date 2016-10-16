package com.hiroshi.cimoc.ui.view;

import android.support.annotation.ColorRes;

import com.hiroshi.cimoc.model.Source;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface SourceView extends BaseView {

    void onSourceLoadSuccess(List<Source> list);

    void onSourceLoadFail();

    void onThemeChange(@ColorRes int primary, @ColorRes int accent);

}
