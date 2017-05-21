package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.component.DialogCaller;
import com.hiroshi.cimoc.model.Comic;

import java.util.List;

/**
 * Created by Hiroshi on 2017/5/14.
 */

public interface LocalView extends GridView, DialogCaller {

    void onLocalDeleteSuccess(long id);

    void onLocalDeleteFail();

    void onLocalAddSuccess(List<Comic> list);

    void onLocalAddFail();

}
