package com.hiroshi.cimoc.component;

import android.os.Bundle;

/**
 * Created by Hiroshi on 2016/12/4.
 */

public interface DialogCaller {

    String EXTRA_DIALOG_RESULT_INDEX = "cimoc.intent.extra.EXTRA_DIALOG_RESULT_INDEX";
    String EXTRA_DIALOG_RESULT_VALUE = "cimoc.intent.extra.EXTRA_DIALOG_RESULT_VALUE";
    String EXTRA_DIALOG_REQUEST_CODE = "cimoc.intent.extra.EXTRA_DIALOG_REQUEST_CODE";
    String EXTRA_DIALOG_TITLE = "cimoc.intent.extra.EXTRA_DIALOG_TITLE";
    String EXTRA_DIALOG_ITEMS = "cimoc.intent.extra.EXTRA_DIALOG_ITEMS";
    String EXTRA_DIALOG_CONTENT = "cimoc.intent.extra.EXTRA_DIALOG_CONTENT";
    String EXTRA_DIALOG_CONTENT_TEXT = "cimoc.intent.extra.EXTRA_DIALOG_CONTENT_TEXT";
    String EXTRA_DIALOG_NEGATIVE = "cimoc.intent.extra.EXTRA_DIALOG_NEGATIVE";
    String EXTRA_DIALOG_CHOICE_ITEMS = "cimoc.intent.extra.EXTRA_DIALOG_CHOICE_ITEMS";

    void onDialogResult(int requestCode, Bundle bundle);

}
