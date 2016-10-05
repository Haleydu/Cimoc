package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.core.parser.NodeIterator;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/10/3.
 */

public class MH57 extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("http://m.57mh.com/search/q_%s-p-%d", keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        for (Node node : body.list("div.book-result > div.pager-cont > span.pager > span.current")) {
            try {
                if (Integer.parseInt(node.text()) < page) {
                    return null;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return new NodeIterator(body.list("#data_list > li")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr("a:eq(0)", "href", "/", 1);
                String title = node.text("a:eq(0) > h3");
                String cover = node.attr("a:eq(0) > div.thumb > img", "data-src");
                String update = node.text("dl:eq(4) > dd");
                String author = node.text("dl:eq(1) > a > dd");
                return new Comic(SourceManager.SOURCE_57MH, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.57mh.com/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.main-bar > h1");
        String cover = body.attr("div.book-detail > div.cont-list > div.thumb > img", "src");
        String update = body.text("div.book-detail > div.cont-list > dl:eq(7) > dd");
        String author = body.text("div.book-detail > div.cont-list > dl:eq(3) > dd");
        String intro = body.text("#bookIntro");
        boolean status = "已完结".equals(body.text("div.book-detail > div.cont-list > div.thumb > i"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#chapterList > ul > li > a")) {
            String title = node.text();
            String path = node.attr("href", "/|\\.", 2);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        if (page == 1) {
            String url = "http://m.57mh.com/latest";
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#data_list > li")) {
            String cid = node.attr("a:eq(1)", "href", "/", 1);
            String title = node.text("a:eq(1) > h3");
            String cover = node.attr("a:eq(1) > div.thumb > img", "data-src");
            String update = node.text("dl:eq(5) > dd");
            String author = node.text("dl:eq(2) > a > dd");
            list.add(new Comic(SourceManager.SOURCE_57MH, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.57mh.com/%s/%s.html", cid, path);
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
                    list.add(new ImageUrl(i + 1, "http://play.333dm.com/get.php?url=".concat(array.getString(i)), true));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder().url(url).build();
    }

    @Override
    public String parseLazy(String html, String url) {
        if (html == null) {
            String path = url.split("=")[1];
            return "http://images.333dm.com".concat(path);
        }
        return url;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.book-detail > div.cont-list > dl:eq(7) > dd");
    }

}
