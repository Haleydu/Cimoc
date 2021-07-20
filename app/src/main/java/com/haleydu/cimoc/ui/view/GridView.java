package com.haleydu.cimoc.ui.view;

import com.haleydu.cimoc.component.DialogCaller;
import com.haleydu.cimoc.component.ThemeResponsive;
import com.haleydu.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/9/30.
 */

public interface GridView extends BaseView, DialogCaller, ThemeResponsive {

    void onComicLoadSuccess(List<Object> list);

    void onComicLoadFail();

    void onExecuteFail();

}
