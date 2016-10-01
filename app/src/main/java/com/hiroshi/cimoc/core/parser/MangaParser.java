package com.hiroshi.cimoc.core.parser;

import com.hiroshi.cimoc.model.Comic;

import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/22.
 */
public abstract class MangaParser implements Parser {

    @Override
    public Request getRecentRequest(int page) {
        return null;
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        return null;
    }

    @Override
    public Request getLazyRequest(String url) {
        return null;
    }

    @Override
    public String parseLazy(String html, String url) {
        return null;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return null;
    }

    @Override
    public String parseCheck(String html) {
        return null;
    }

    @Override
    public Request getBeforeImagesRequest() {
        return null;
    }

    @Override
    public void beforeImages(String html) {}

}
