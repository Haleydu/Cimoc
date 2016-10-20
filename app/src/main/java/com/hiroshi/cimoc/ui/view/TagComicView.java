package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.MiniComic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/10/11.
 */

public interface TagComicView extends BaseView {

    void onTagComicLoadSuccess(List<MiniComic> list);

    void onTagComicLoadFail();

    void onComicLoadSuccess(List<MiniComic> list);

    void onComicLoadFail();

    void onComicInsertSuccess();

    void onComicInsertFail();

    void onComicUnFavorite(long id);

    void onComicFavorite(MiniComic comic);

    void onTagUpdateInsert(MiniComic comic);

    void onTagUpdateDelete(MiniComic comic);

}
