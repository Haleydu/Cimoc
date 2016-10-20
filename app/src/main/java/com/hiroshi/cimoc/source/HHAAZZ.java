package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.core.parser.NodeIterator;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/26.
 */
public class HHAAZZ extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = "http://hhaazz.com/comicsearch/s.aspx?s=".concat(keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("ul.se-list > li")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr("a.pic", "href", "/", 4);
                String title = node.text("a.pic > div > h3");
                String cover = node.attr("a.pic > img", "src");
                String update = node.text("a.pic > div > p:eq(4) > span", 0, 10);
                String author = node.text("a.pic > div > p:eq(1)");
                // boolean status = node.text("a.tool > span.h").contains("完结");
                return new Comic(SourceManager.SOURCE_HHAAZZ, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://hhaazz.com/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public String parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.main > div > div.pic > div.con > h3");
        String cover = body.attr("div.main > div > div.pic > img", "src");
        String update = body.text("div.main > div > div.pic > div.con > p:eq(5)", 5);
        String author = body.text("div.main > div > div.pic > div.con > p:eq(1)", 3);
        String intro = body.text("#detail_block > div > p");
        boolean status = body.text("div.main > div > div.pic > div.con > p:eq(4)").contains("完结");
        comic.setInfo(title, cover, update, intro, author, status);

        return null;
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#sort_div_p > a")) {
            String title = node.attr("title");
            String path = node.attr("href").substring(17);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = StringUtils.format("http://hhaazz.com/dfcomiclist_%d.htm", page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("li > a.pic")) {
            String cid = node.attr("href", "/", 4);
            String title = node.text("div.con > h3");
            String cover = node.attr("img", "src");
            String update = node.text("div.con > p > span", 0, 10);
            String author = node.text("div.con > p:eq(1)");
            list.add(new Comic(SourceManager.SOURCE_HHAAZZ, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Request getBeforeImagesRequest() {
        if (server != null) {
            return null;
        }
        return new Request.Builder().url("http://hhaazz.com/js/ds.js").build();
    }

    @Override
    public void beforeImages(String html) {
        if (html != null) {
            String str = StringUtils.match("sDS = \"(.*?)\";", html, 1);
            if (str != null) {
                server = str.split("\\|");
            }
        }
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = "http://hhaazz.com/".concat(path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        if (server != null) {
            String[] str = StringUtils.match("sFiles=\"(.*?)\";var sPath=\"(\\d+)\"", html, 1, 2);
            if (str != null) {
                String[] result = unsuan(str[0]);
                for (int i = 0; i != result.length; ++i) {
                    list.add(new ImageUrl(i + 1, server[Integer.parseInt(str[1]) - 1].concat(result[i]), false));
                }
            }
        }
        return list;
    }

    private String[] unsuan(String str) {
        int num = str.length() - str.charAt(str.length() - 1) + 'a';
        String code = str.substring(num - 13, num - 3);
        String cut = str.substring(num - 3, num - 2);
        str = str.substring(0, num - 13);
        for (int i = 0; i < 10; ++i) {
            str = str.replace(code.charAt(i), (char) ('0' + i));
        }
        StringBuilder builder = new StringBuilder();
        String[] array = str.split(cut);
        for (int i = 0; i != array.length; ++i) {
            builder.append((char) Integer.parseInt(array[i]));
        }
        return builder.toString().split("\\|");
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.main > div > div.pic > div:eq(1) > p:eq(5)", 5);
    }

}
