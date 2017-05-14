package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.component.DialogCaller;
import com.hiroshi.cimoc.model.Local;

import java.util.List;

/**
 * Created by Hiroshi on 2017/5/14.
 */

public interface LocalView extends BaseView, DialogCaller {

    void onDataLoadSuccess(List<Local> list);

    void onDataLoadFail();

    void onDataDeleteSuccess();

    void onDataDeleteFail();

    void onLocalAddSuccess();

}
