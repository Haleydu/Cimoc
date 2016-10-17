package com.hiroshi.cimoc.ui.view;

import android.support.annotation.ColorRes;

import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/9/30.
 */

public interface GridView extends BaseView {

    void onComicLoadSuccess(List<MiniComic> list);

    void onComicLoadFail();

    void onThemeChange(@ColorRes int primary, @ColorRes int accent);

}
