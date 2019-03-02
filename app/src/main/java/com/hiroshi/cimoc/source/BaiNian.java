package com.hiroshi.cimoc.source;

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

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Hiroshi on 2016/10/3.
 */

public class BaiNian extends MangaParser {

    public static final int TYPE = 57;
    public static final String DEFAULT_TITLE = "百年漫画";

    private static final String[] servers = {
            "http://images.lancaier.com"
    };

    public BaiNian(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page > 1) return null;

        RequestBody postData = new FormBody.Builder()
                .add("wd", keyword)
                .build();

        return new Request.Builder().url("https://m.bnmanhua.com/index.php?m=vod-search").post(postData).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);

        return new NodeIterator(body.list("ui.tbox_m > li.vbox")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit("a.vbox_t", 0);
                String title = node.attr("a.vbox_t", "title");
                String cover = "https://m.bnmanhua.com" + node.attr("mip-img", "src");
                String update = node.text("h4.red");
                String author = "";
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "http://m.wuqimh.com/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.wuqimh.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.wuqimh.com/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.main-bar > h1");
        String cover = body.src("div.book-detail > div.cont-list > div.thumb > img");
        String update = body.text("div.book-detail > div.cont-list > dl:eq(7) > dd");
        String author = body.text("div.book-detail > div.cont-list > dl:eq(3) > dd");
        String intro = body.text("#bookIntro");
        boolean status = isFinish(body.text("div.book-detail > div.cont-list > div.thumb > i"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#chapterList > ul > li > a")) {
            String title = node.text();
            String path = node.hrefWithSplit(1);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.wuqimh.com/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String packed = StringUtils.match("eval(.*?)\\n", html, 1);
        if (packed != null) {
            String result = DecryptionUtils.evalDecrypt(packed);
            String jsonString = StringUtils.match("'fs':\\s*(\\[.*?\\])", result, 1);
            try {
                JSONArray array = new JSONArray(jsonString);
                int size = array.length();
                for (int i = 0; i != size; ++i) {
                    String url = array.getString(i);
                    if (url.indexOf("http://") == -1) {
                        url = servers[0] + url;
                    }
                    list.add(new ImageUrl(i + 1, url, false));
                }
            } catch (Exception e) {
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
        return new Node(html).text("div.book-detail > div.cont-list > dl:eq(7) > dd");
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new ArrayList<>();
        Node body = new Node(html);
        for (Node node : body.list("span.pager > span.current")) {
            try {
                if (Integer.parseInt(node.text()) < page) {
                    return list;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        for (Node node : body.list("#contList > li")) {
            String cid = node.hrefWithSplit("a", 0);
            String title = node.attr("a", "title");
            String cover = node.attr("a > img", "data-src");
            String update = node.textWithSubstring("span.updateon", 4, 14);
            list.add(new Comic(TYPE, cid, title, cover, update, null));
        }
        return list;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.wuqimh.com/");
    }

}
