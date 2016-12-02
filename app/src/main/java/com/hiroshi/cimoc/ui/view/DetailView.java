package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface DetailView extends BaseView {

    void onComicLoadSuccess(Comic comic);

    void onChapterLoadSuccess(List<Chapter> list);

    void onChapterChange(String chapter);

    void onDownloadLoadFail();

    void onParseError();

    void onNetworkError();

    void onTagOpenSuccess();

    void onTagOpenFail();

    void onLastOpenSuccess(String path);

    void onFavoriteSuccess();

    void onUnfavoriteSuccess();

}
