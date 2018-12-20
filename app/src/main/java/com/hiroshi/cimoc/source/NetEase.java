package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.JsonIterator;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

//import com.google.gson.JsonObject;

/**
 * Created by FEILONG on 2017/12/21.
 */

public class NetEase extends MangaParser {

    public static final int TYPE = 53;
    public static final String DEFAULT_TITLE = "网易漫画";

    public NetEase(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = StringUtils.format("https://h5.manhua.163.com/search/book/key/data.json?key=%s&page=%d&pageSize=10&target=3",
                keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        try {
            JSONObject js = new JSONObject(html);
            return new JsonIterator(js.getJSONArray("records")) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        String cid = object.getString("bookId");
                        String title = object.getString("title");
                        String cover = object.getString("cover");
                        String author = object.getString("author");
                        return new Comic(TYPE, cid, title, cover, null, author);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getUrl(String cid) {
        return "https://h5.manhua.163.com/source/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("h5.manhua.163.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://h5.manhua.163.com/source/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String update = "null";
        String title = body.text(".sr-detail__heading");
        String intro = body.attr(".share-button", "summary");
        String author = body.text(".sr-detail__author-text");
        String cover = body.attr(".share-button", "pic");
        comic.setInfo(title, cover, update, intro, author, true);
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        String url = StringUtils.format("https://h5.manhua.163.com/book/catalog/%s.json", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        try {
            JSONObject js = new JSONObject(html);
            JSONArray jsarr = js.getJSONObject("catalog")
                    .getJSONArray("sections")
                    .getJSONObject(0)
                    .getJSONArray("sections");

            for (int i = 0; i < jsarr.length(); i++) {
                String title = jsarr.getJSONObject(i).getString("title");
                String path = jsarr.getJSONObject(i).getString("sectionId");
                list.add(new Chapter(title, path));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        //https://h5.manhua.163.com/reader/section/4458002705630123099/4457282705880101050.json
        String url = StringUtils.format("https://h5.manhua.163.com/reader/section/%s/%s.json", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();

        try {
            JSONObject js = new JSONObject(html);
            JSONArray jsarr = js.getJSONArray("images");

            for (int i = 0; i < jsarr.length(); i++) {
                list.add(new ImageUrl(i + 1, jsarr.getJSONObject(i).getString("highUrl"), false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.book-detail > div.cont-list > dl:eq(2) > dd");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "https://h5.manhua.163.com");
    }

}
