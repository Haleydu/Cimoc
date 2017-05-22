package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.component.DialogCaller;
import com.hiroshi.cimoc.component.ThemeResponsive;
import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/9/30.
 */

public interface GridView extends BaseView, DialogCaller, ThemeResponsive {

    void onComicLoadSuccess(List<MiniComic> list);

    void onComicLoadFail();

    void onExecuteFail();

}
