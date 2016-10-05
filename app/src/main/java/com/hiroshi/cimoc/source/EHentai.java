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
public class EHentai extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("http://g.e-hentai.org/?f_search=%s&page=%d", keyword, (page - 1));
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("div.ido > div > table.itg > tbody > tr[class^=gtr]")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr("td:eq(2) > div > div:eq(2) > a", "href");
                cid = cid.substring(24, cid.length() - 1);
                String title = node.text("td:eq(2) > div > div:eq(2) > a");
                String cover = node.attr("td:eq(2) > div > div:eq(0) > img", "src");
                if (cover == null) {
                    String temp = node.text("td:eq(2) > div > div:eq(0)", 14).split("~", 2)[0];
                    cover = "http://ehgt.org/".concat(temp);
                }
                String update = node.text("td:eq(1)", 0, 10);
                String author = StringUtils.match("\\[(.*?)\\]", title, 1);
                title = title.replaceFirst("\\[.*?\\]\\s*", "");
                return new Comic(SourceManager.SOURCE_EHENTAI, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://g.e-hentai.org/g/%s", cid);
        return new Request.Builder().url(url).header("Cookie", "nw=1").build();
    }

    @Override
    public String parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String update = body.text("#gdd > table > tbody > tr:eq(0) > td:eq(1)", 0, 10);
        String title = body.text("#gn");
        String intro = body.text("#gj");
        String author = body.text("#taglist > table > tbody > tr > td:eq(1) > div > a[id^=ta_artist]");
        String cover = body.attr("#gd1 > img", "src");
        comic.setInfo(title, cover, update, intro, author, true);

        return null;
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        String length = body.text("#gdd > table > tbody > tr:eq(5) > td:eq(1)", " ", 0);
        int size = Integer.parseInt(length) % 40 == 0 ? Integer.parseInt(length) / 40 : Integer.parseInt(length) / 40 + 1;
        for (int i = 0; i != size; ++i) {
            list.add(0, new Chapter("Ch" + i, String.valueOf(i)));
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = StringUtils.format("http://g.e-hentai.org/?page=%d", (page - 1));
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("table.itg > tbody > tr[class^=gtr]")) {
            String cid = node.attr("td:eq(2) > div > div:eq(2) > a", "href");
            cid = cid.substring(24, cid.length() - 1);
            String title = node.text("td:eq(2) > div > div:eq(2) > a");
            String cover = node.attr("td:eq(2) > div > div:eq(0) > img", "src");
            if (cover == null) {
                String temp = node.text("td:eq(2) > div > div:eq(0)", 14).split("~", 2)[0];
                cover = "http://ehgt.org/".concat(temp);
            }
            String update = node.text("td:eq(1)", 0, 10);
            String author = StringUtils.match("\\[(.*?)\\]", title, 1);
            title = title.replaceFirst("\\[.*?\\]\\s*", "");
            list.add(new Comic(SourceManager.SOURCE_EHENTAI, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://g.e-hentai.org/g/%s/?p=%s", cid, path);
        return new Request.Builder().url(url).header("Cookie", "nw=1").build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        Node body = new Node(html);
        int count = 0;
        for (Node node : body.list("#gdt > div > div > a")) {
            list.add(new ImageUrl(++count, node.attr("href"), true));
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
            return null;
        }
        return new Node(html).attr("a > img[style]", "src");
    }

}
