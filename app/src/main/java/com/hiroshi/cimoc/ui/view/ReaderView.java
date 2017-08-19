package com.hiroshi.cimoc.ui.view;

import android.net.Uri;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.ImageUrl;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface ReaderView extends BaseView {

    void onParseError();

    void onNextLoadNone();

    void onPrevLoadNone();

    void onNextLoading();

    void onPrevLoading();

    void onNextLoadSuccess(List<ImageUrl> list);

    void onPrevLoadSuccess(List<ImageUrl> list);

    void onInitLoadSuccess(List<ImageUrl> list, int progress, int source, boolean local);

    void onChapterChange(Chapter chapter);

    void onImageLoadSuccess(int id, String url);

    void onImageLoadFail(int id);

    void onPictureSaveSuccess(Uri uri);

    void onPictureSaveFail();

    void onPicturePaging(ImageUrl image);

}
