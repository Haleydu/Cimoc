package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.ImageUrl;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface ReaderView extends BaseView {

    void showMessage(int resId);

    void onParseError();

    void onNetworkError();

    void onNextLoadSuccess(List<ImageUrl> list);

    void onPrevLoadSuccess(List<ImageUrl> list);

    void onInitLoadSuccess(List<ImageUrl> list, int progress);

    void onChapterChange(Chapter chapter);

    void onImageLoadSuccess(int id, String url);

    void onImageLoadFail(int id);

}
