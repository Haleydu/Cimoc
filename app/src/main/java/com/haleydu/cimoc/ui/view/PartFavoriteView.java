package com.haleydu.cimoc.ui.view;

import com.haleydu.cimoc.component.DialogCaller;
import com.haleydu.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface PartFavoriteView extends BaseView, DialogCaller {

    void onComicLoadSuccess(List<Object> list);

    void onComicLoadFail();

    void onComicTitleLoadSuccess(List<String> list);

    void onComicTitleLoadFail();

    void onComicInsertSuccess(List<Object> list);

    void onComicInsertFail();

    void onComicAdd(MiniComic comic);

    void onComicRead(MiniComic comic);

    void onComicRemove(long id);

    void onHighlightCancel(MiniComic comic);

}
