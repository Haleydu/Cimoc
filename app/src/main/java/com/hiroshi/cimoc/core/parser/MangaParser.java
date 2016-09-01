package com.hiroshi.cimoc.core.parser;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/22.
 */
public abstract class MangaParser implements Parser {

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

}
