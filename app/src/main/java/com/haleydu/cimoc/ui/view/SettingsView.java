package com.haleydu.cimoc.ui.view;

import com.haleydu.cimoc.component.DialogCaller;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface SettingsView extends BaseView, DialogCaller {

    void onFileMoveSuccess();

    void onExecuteSuccess();

    void onExecuteFail();

}
