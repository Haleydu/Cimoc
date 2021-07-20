package com.haleydu.cimoc.ui.view;

import com.haleydu.cimoc.component.ThemeResponsive;
import com.haleydu.cimoc.model.Source;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface SourceView extends BaseView, ThemeResponsive {

    void onSourceLoadSuccess(List<Source> list);

    void onSourceLoadFail();

}
