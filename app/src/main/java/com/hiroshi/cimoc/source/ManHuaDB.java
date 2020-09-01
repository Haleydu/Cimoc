package com.hiroshi.cimoc.source;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by ZhiWen on 2019/02/25.
 */

public class ManHuaDB extends MangaParser {

    public static final int TYPE = 46;
    public static final String DEFAULT_TITLE = "漫画DB";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public ManHuaDB(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("https://www.manhuadb.com/search?q=%s", keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public String getUrl(String cid) {
        return "https://www.manhuadb.com/manhua/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.manhuadb.com"));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("a.d-block")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(1);
                String title = node.attr("title");
                String cover = node.attr("img", "data-original");
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://www.manhuadb.com/manhua/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("h1.comic-title");
//        String cover = body.src("div.cover > img"); // 这一个封面可能没有
        String cover = body.src("td.comic-cover > img");
        String author = body.text("a.comic-creator");
        String intro = body.text("p.comic_story");
        boolean status = isFinish(body.text("a.comic-pub-state"));

        String update = body.text("a.comic-pub-end-date");
        if (update == null || update.equals("")) {
            update = body.text("a.comic-pub-date");
        }
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("#comic-book-list > div > ol > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://www.manhuadb.com/manhua/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new ArrayList<>();

        try {
            final String imageHost = StringUtils.match("data-host=\"(.*?)\"", html, 1);
            final String imagePre = StringUtils.match("data-img_pre=\"(.*?)\"", html, 1);
            final String base64Data = StringUtils.match("var img_data = '(.*?)';", html, 1);
            final String jsonStr = DecryptionUtils.base64Decrypt(base64Data);
            final JSONArray imageList = JSON.parseArray(jsonStr);

            for(int i = 0; i < imageList.size(); i++ ) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);

                final JSONObject image = imageList.getJSONObject(i);
                final String imageUrl = imageHost + imagePre + image.getString("img");

                list.add(new ImageUrl(id, comicChapter, image.getIntValue("p"), imageUrl, false));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
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
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        // 这里表示的是更新时间
        Node body = new Node(html);
        String update = body.text("a.comic-pub-end-date");
        if (update == null || update.equals("")) {
            update = body.text("a.comic-pub-date");
        }
        return update;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "https://www.manhuadb.com");
    }

}
