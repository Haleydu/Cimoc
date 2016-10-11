package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.MiniComic;

import java.util.Collection;

/**
 * Created by Hiroshi on 2016/9/30.
 */

public interface GridView extends BaseView {

    void onComicLoadSuccess(Collection<MiniComic> list);

    void onComicLoadFail();

    void onComicFilterSuccess(Collection<MiniComic> list);

    void onComicFilterFail();

}
