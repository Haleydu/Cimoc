package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.component.DialogCaller;
import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface PartFavoriteView extends BaseView, DialogCaller {

    void onComicLoadSuccess(List<MiniComic> list);

    void onComicLoadFail();

    void onComicTitleLoadSuccess(List<String> list);

    void onComicTitleLoadFail();

    void onComicInsertSuccess(List<MiniComic> list);

    void onComicInsertFail();

    void onComicAdd(MiniComic comic);

    void onComicRead(MiniComic comic);

    void onComicRemove(long id);

    void onHighlightCancel(MiniComic comic);

}
