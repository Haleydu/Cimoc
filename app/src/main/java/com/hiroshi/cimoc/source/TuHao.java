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
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by ZhiWen on 2019/02/25.
 */

public class TuHao extends MangaParser {

    public static final int TYPE = 24;
    public static final String DEFAULT_TITLE = "土豪漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public TuHao(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("https://m.tohomh123.com/action/Search?keyword=%s", keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public String getUrl(String cid) {
        return "https://m.tohomh123.com/".concat(cid).concat("/");
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.tohomh123.com", "\\w+", 0));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#classList_1 > ul > li")) {
            @Override
            protected Comic parse(Node node) {

                String title = node.attr("a", "title");
                String urls = node.attr("a", "href");
                String cid = urls.substring(1, urls.length() - 1);
                String cover = node.attr("a > div > img", "src");
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://m.tohomh123.com/".concat(cid).concat("/");
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String cover = body.src("div.coverForm > img");
        String intro = body.text("div.detailContent > p");
        String title = body.text("div.detailForm > div > div > h1");

        String update = "";
        String author = "";
        List<Node> upDateAndAuth = body.list("div.detailForm > div > div > p");

        if (upDateAndAuth.size() == 5) {
            update = upDateAndAuth.get(3).text().substring(5).trim();
            author = upDateAndAuth.get(2).text().substring(3).trim();
        } else {
            update = upDateAndAuth.get(2).text().substring(5).trim();
            author = upDateAndAuth.get(1).text().substring(3).trim();
        }

        // 连载状态
        boolean status = isFinish("连载");
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#chapterList_1 > ul > li > a")) {
            String title = node.text();
            String path = node.hrefWithSplit(1);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://m.tohomh123.com/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();

        String str = StringUtils.match("var pl = \'(.*?)\'", html, 1);
        // 得到 https://mh2.wan1979.com/upload/jiemoren/1989998/
        String prevStr = str.substring(0, str.length() - 8);

        // 得到 0000
        int lastStr = Integer.parseInt(str.substring(str.length() - 8, str.length() - 4));
        int pagNum = Integer.parseInt(StringUtils.match("var pcount=(.*?);", html, 1));

        if (str != null) {
            try {
                for (int i = lastStr; i < pagNum + lastStr; i++) {
                    String url = StringUtils.format("%s%04d.jpg", prevStr, i);
//                  https://mh2.wan1979.com/upload/jiemoren/1989998/0000.jpg
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
        // 这里表示的是更新时间
        Node body = new Node(html);

        String update = "";
        List<Node> upDateAndAuth = body.list("div.detailForm > div > div > p");

        if (upDateAndAuth.size() == 5) {
            update = upDateAndAuth.get(3).text().substring(5).trim();
        } else {
            update = upDateAndAuth.get(2).text().substring(5).trim();
        }
        return update;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "https://m.tohomh123.com");
    }

}
