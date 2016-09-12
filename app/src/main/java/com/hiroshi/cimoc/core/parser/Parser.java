package com.hiroshi.cimoc.core.parser;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;

import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/22.
 */
public interface Parser {

    Request getSearchRequest(String keyword, int page);

    List<Comic> parseSearch(String html, int page);

    Request getInfoRequest(String cid);

    List<Chapter> parseInfo(String html, Comic comic);

    Request getBeforeImagesRequest();

    void beforeImages(String html);

    Request getImagesRequest(String cid, String path);

    List<ImageUrl> parseImages(String html);

    Request getLazyRequest(String url);

    String parseLazy(String html, String url);

    Request getCheckRequest(String cid);

    String parseCheck(String html);

}
