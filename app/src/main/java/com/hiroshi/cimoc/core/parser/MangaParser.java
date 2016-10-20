package com.hiroshi.cimoc.core.parser;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/22.
 */
public abstract class MangaParser implements Parser {

    protected String[] server;

    @Override
    public Request getChapterRequest(String cid) {
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

    protected String[] buildUrl(String path) {
        if (server != null) {
            String[] url = new String[server.length];
            for (int i = 0; i != server.length; ++i) {
                url[i] = server[i].concat(path);
            }
            return url;
        }
        return null;
    }

}
