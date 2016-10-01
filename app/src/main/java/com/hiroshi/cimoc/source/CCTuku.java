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

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/28.
 */
public class CCTuku extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("http://m.tuku.cc/comic/search?word=%s&page=%d", keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        int total = Integer.parseInt(StringUtils.match("\\d+", body.text("div.title-banner > div > h1"), 0));
        if (page > total) {
            return null;
        }
        return new NodeIterator(body.list("div.main-list > div > div > div")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr("div:eq(1) > div:eq(0) > a", "href", "/", 2);
                String title = node.text("div:eq(1) > div:eq(0) > a");
                String cover = node.attr("div:eq(0) > a > img", "src");
                String update = node.text("div:eq(1) > div:eq(1) > dl:eq(3) > dd > font");
                String author = node.text("div:eq(1) > div:eq(1) > dl:eq(1) > dd > a");
                return new Comic(SourceManager.SOURCE_CCTUKU, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.tuku.cc/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseInfo(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("ul.list-body > li > a")) {
            String c_title = node.text();
            String c_path = node.attr("href", "/", 3);
            list.add(new Chapter(c_title, c_path));
        }

        String title = body.text("div.title-banner > div.book-title > h1", 0, -3);
        String cover = body.attr("div.book > div > div.row > div:eq(0) > a > img", "src");
        String update = body.text("div.book > div > div.row > div:eq(1) > div > dl:eq(5) > dd > font", 0, 10);
        String author = body.text("div.book > div > div.row > div:eq(1) > div > dl:eq(1) > dd > a");
        String intro = body.text("div.book-details > p:eq(1)");
        boolean status = "完结".equals(body.text("div.book > div > div.row > div:eq(0) > div"));
        comic.setInfo(title, cover, update, intro, author, status);

        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.tuku.cc/comic/%s/%s", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String[] rs = StringUtils.match("serverUrl = '(.*?)'[\\s\\S]*?eval(.*?)\\n;", html, 1, 2);
        if (rs != null) {
            try {
                String result = DecryptionUtils.evalDecrypt(rs[1]);
                String[] array = StringUtils.match("pic_url='(.*?)';.*?tpf=(\\d+?);.*pages=(\\d+?);.*?pid=(.*?);.*?pic_extname='(.*?)';", result, 1, 2, 3, 4, 5);
                if (array != null) {
                    int tpf = Integer.parseInt(array[1]) + 1;
                    int pages = Integer.parseInt(array[2]);
                    String format = rs[0] + "/" + array[3] + "/" + array[0] + "/%0" + tpf + "d." + array[4];
                    for (int i = 0; i != pages; ++i) {
                        list.add(new ImageUrl(i + 1, StringUtils.format(format, i + 1), false));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = StringUtils.format("http://m.tuku.cc/newest/%d", page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        int total = Integer.parseInt(StringUtils.match("\\d+", body.text("div.title-banner > div > h1"), 0));
        if (page <= total) {
            for (Node node : body.list("div.main-list > div > div > div")) {
                String cid = node.attr("div:eq(1) > div:eq(0) > a", "href", "/", 2);
                String title = node.text("div:eq(1) > div:eq(0) > a");
                String cover = node.attr("div:eq(0) > a > img", "src");
                String update = node.text("div:eq(1) > div:eq(1) > dl:eq(3) > dd > font");
                String author = node.text("div:eq(1) > div:eq(1) > dl:eq(1) > dd > a");
                list.add(new Comic(SourceManager.SOURCE_CCTUKU, cid, title, cover, update, author));
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
        return new Node(html).text("div.book > div > div:eq(0) > div:eq(1) > div > dl:eq(5) > dd > font", 0, 10);
    }

}
