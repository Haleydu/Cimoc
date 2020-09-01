package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.SearchIterator;

import org.json.JSONException;

import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2017/3/21.
 */

public class Null extends MangaParser {

    public static final int TYPE = -1;
    public static final String DEFAULT_TITLE = "(null)";
    public static final String DEFAULT_SERVER = null;

    public Null() {
        mTitle = DEFAULT_TITLE;
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        return null;
    }

    @Override
    public Request getInfoRequest(String cid) {
        return null;
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        return null;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) throws JSONException {
        return null;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        return null;
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        return null;
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) throws Manga.NetworkErrorException, JSONException {
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
    public List<Comic> parseCategory(String html, int page) {
        return null;
    }

    @Override
    public Headers getHeader() {
        return null;
    }

    @Override
    public String getUrl(String cid) {
        return null;
    }
}
