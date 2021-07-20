package com.haleydu.cimoc.ui.view;

import com.haleydu.cimoc.component.DialogCaller;
import com.haleydu.cimoc.component.ThemeResponsive;
import com.haleydu.cimoc.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface ComicView extends BaseView, ThemeResponsive, DialogCaller {

    void onTagLoadSuccess(List<Tag> list);

    void onTagLoadFail();

}
