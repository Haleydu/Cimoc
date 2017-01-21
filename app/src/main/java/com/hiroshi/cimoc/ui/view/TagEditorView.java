package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.model.Tag;

import java.util.List;

/**
 * Created by Hiroshi on 2016/12/2.
 */

public interface TagEditorView extends BaseView {

    void onTagLoadSuccess(List<Pair<Tag, Boolean>> list);

    void onTagLoadFail();

    void onTagUpdateSuccess();

    void onTagUpdateFail();

}
