package com.hiroshi.cimoc.parser;

import android.net.Uri;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/22.
 */
public abstract class MangaParser implements Parser {

    protected String mTitle;
    protected List<UrlFilter> filter = new ArrayList<>();
    private Category mCategory;

    protected void init(Source source, Category category) {
        mTitle = source.getTitle();
        mCategory = category;

        initUrlFilterList();
    }

    protected void initUrlFilterList() {
//        filter.add(new UrlFilter("manhua.dmzj.com", "/(\\w+)", 1));
    }

    @Override
    public List<Chapter> parseChapter(String html) throws JSONException {
        return null;
    }

    @Override
    public List<ImageUrl> parseImages(String html) throws Manga.NetworkErrorException, JSONException {
        return null;
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
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
    public Category getCategory() {
        return mCategory;
    }

    @Override
    public Request getCategoryRequest(String format, int page) {
        String url = StringUtils.format(format, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        return null;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    protected String[] buildUrl(String path, String[] servers) {
        if (servers != null) {
            String[] url = new String[servers.length];
            for (int i = 0; i != servers.length; ++i) {
                url[i] = servers[i].concat(path);
            }
            return url;
        }
        return null;
    }

    protected boolean isFinish(String text) {
        return text != null && (text.contains("完结") || text.contains("Completed") || text.contains("完結"));
    }

    @Override
    public String getUrl(String cid) {
        return cid;
    }

    @Override
    public Headers getHeader() {
        return null;
    }

    @Override
    public Headers getHeader(String url) {
        return getHeader();
    }

    @Override
    public Headers getHeader(List<ImageUrl> list) {
        return getHeader();
    }

    @Override
    public boolean isHere(Uri uri) {
        boolean val = false;
        for (UrlFilter uf : filter) {
            val |= (uri.getHost().indexOf(uf.Filter) != -1);
        }
        return val;
    }

    @Override
    public String getComicId(Uri uri) {
        for (UrlFilter uf : filter) {
            if (uri.getHost().indexOf(uf.Filter) != -1) {
                return StringUtils.match(uf.Regex, uri.getPath(), uf.Group);
            }
        }
        return null;
    }

}
