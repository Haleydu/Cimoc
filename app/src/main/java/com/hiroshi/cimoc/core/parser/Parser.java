package com.hiroshi.cimoc.core.parser;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Pair;

import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/22.
 */
public interface Parser {

    Request getSearchRequest(String keyword, int page);

    SearchIterator getSearchIterator(String html, int page);

    Request getInfoRequest(String cid);

    void parseInfo(String html, Comic comic);

    Request getChapterRequest(String html, String cid);

    List<Chapter> parseChapter(String html);

    Request getImagesRequest(String cid, String path);

    List<ImageUrl> parseImages(String html);

    Request getRecentRequest(int page);

    List<Comic> parseRecent(String html, int page);

    Request getLazyRequest(String url);

    String parseLazy(String html, String url);

    Request getCheckRequest(String cid);

    String parseCheck(String html);

    Request getCategoryRequest(String id, int page);

    List<Comic> parseCategory(String html);

}
