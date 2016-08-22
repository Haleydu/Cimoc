package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface FavoriteView extends BaseView {

    void onItemAdd(MiniComic comic);

    void onItemAdd(List<MiniComic> list);

    void onItemRemove(long id);

    void onSourceRemove(int source);

    void onCheckComplete(List<MiniComic> list);

    void onProgressChange(int progress, int max);

}
