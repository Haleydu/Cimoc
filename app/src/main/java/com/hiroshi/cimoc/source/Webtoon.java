package com.hiroshi.cimoc.source;

import android.util.Pair;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaCategory;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Hiroshi on 2016/9/29.
 */

public class Webtoon extends MangaParser {

    public static final int TYPE = 6;
    public static final String DEFAULT_TITLE = "Webtoon";

    public Webtoon(Source source) {
        init(source, new Category());
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = "https://m.webtoons.com/zh-hant/search";
            RequestBody body = new FormBody.Builder().add("keyword", keyword).add("searchType", "ALL").build();
            return new Request.Builder().url(url).post(body).addHeader("Referer", "https://m.webtoons.com").build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("ul._searchResultList > li > a")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(-1);
                String title = node.text("div.row > div.info > p.subj > span");
                String cover = node.src("div.row > div.pic > img");
                String author = node.text("div.row > div.info > p.author");
                return new Comic(TYPE, cid, title, cover, null, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "https://m.webtoons.com/episodeList?titleNo=".concat(cid);
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://m.webtoons.com/episodeList?titleNo=".concat(cid);
        return new Request.Builder().url(url).addHeader("Referer", "http://m.webtoons.com").build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("#ct > div.detail_info > a._btnInfo > p.subj");
        String cover = body.src("#_episodeList > li > a > div.row > div.pic > img");
        String update = body.text("#_episodeList > li > a > div.row > div.info > p.date");
        if (update != null) {
            String[] args = update.split("\\D");
            update = StringUtils.format("%4d-%02d-%02d", Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
        String author = body.text("#ct > div.detail_info > a._btnInfo > p.author");
        String intro = body.text("#_informationLayer > p.summary_area");
        boolean status = isFinish(body.text("#_informationLayer > div.info_update"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int i=0;
        for (Node node : body.list("#_episodeList > li > a")) {
            String title = node.text("div.row > div.info > p.sub_title > span");
            String path = node.hrefWithSubString(30);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = "https://m.webtoons.com/zh-hant/".concat(path);
        return new Request.Builder().url(url).addHeader("Referer", "https://m.webtoons.com").build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        String jsonString = StringUtils.match("var imageList = ([\\s\\S]*?);", html, 1);
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                int size = array.length();
                for (int i = 0; i != size; ++i) {
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    JSONObject object = array.getJSONObject(i);
                    list.add(new ImageUrl(id, comicChapter, i + 1, object.getString("url"), false));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        String update = new Node(html).text("#_episodeList > li > a > div.row > div.info > p.date");
        if (update != null) {
            String[] args = update.split("\\D");
            update = StringUtils.format("%4d-%02d-%02d", Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        }
        return update;
    }

    @Override
    public Request getCategoryRequest(String format, int page) {
        if (page == 1) {
            return new Request.Builder().url(format).addHeader("Referer", "https://m.webtoons.com").build();
        }
        return null;
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new ArrayList<>();
        Node body = new Node(html);
        for (Node node : body.list("#ct > ul > li > a")) {
            String cid = node.hrefWithSplit(-1);
            String title = node.text("div.info > p.subj > span");
            String cover = node.attrWithSplit("div.pic", "style", "\\(|\\)", 1);
            list.add(new Comic(TYPE, cid, title, cover, null, null));
        }
        return list;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "https://m.webtoons.com");
    }

    private static class Category extends MangaCategory {

        @Override
        public String getFormat(String... args) {
            return "https://m.webtoons.com/zh-hant/new";
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("新作推荐", ""));
            return list;
        }

    }

}
