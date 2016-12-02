package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface TagComicView extends BaseView {

    void onComicLoadSuccess(List<MiniComic> list);

    void onComicLoadFail();

    void onComicInsertSuccess(List<MiniComic> list);

    void onComicInsertFail();

    void onComicUnFavorite(long id);

    void onTagComicInsert(MiniComic comic);

    void onTagComicDelete(MiniComic comic);

}
