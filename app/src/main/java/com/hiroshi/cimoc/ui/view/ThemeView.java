package com.hiroshi.cimoc.ui.view;

import android.support.annotation.ColorRes;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public interface ThemeView {

    void onThemeChange(@ColorRes int primary, @ColorRes int accent);

}
