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
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                String cover = node.attr("img", "src");
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
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
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
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#comic-book-list > div > ol > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);
            list.add(0, new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://www.manhuadb.com/manhua/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new ArrayList<>();
        Node body = new Node(html);

        // 获取本章的总页数
        String pageStr = body.text("ol.breadcrumb > li:eq(2)");
        Matcher pageNumMatcher = Pattern.compile("共\\s*(\\d+)").matcher(pageStr);
        if (pageNumMatcher.find()) {
            final int page = Integer.parseInt(pageNumMatcher.group(1));
            String path = body.attr("ol.breadcrumb > li:eq(2) > a", "href");
            path = path.substring(1, path.length() - 5);

            list.add(new ImageUrl(1, StringUtils.format("https://www.manhuadb.com/%s_p.html", path), true));
            for (int i = 2; i <= page; ++i) {
                list.add(new ImageUrl(i, StringUtils.format("https://www.manhuadb.com/%s_p%d.html", path, i), true));
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
        Node body = new Node(html);
        return body.attr("div.text-center > img.img-fluid", "src");
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
