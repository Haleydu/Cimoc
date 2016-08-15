package com.hiroshi.cimoc.core.source;

import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/28.
 */
public class CCTuku extends Manga {

    public CCTuku() {
        super(SourceManager.SOURCE_CCTUKU, "http://m.tuku.cc");
    }

    @Override
    protected Request buildSearchRequest(String keyword, int page) {
        String url = host + "/comic/search?word=" + keyword + "&page=" + page;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Comic> parseSearch(String html, int page) {
        Node body = MachiSoup.body(html);
        int total = Integer.parseInt(MachiSoup.match("\\d+", body.text("div.title-banner > div > h1"), 0));
        if (page > total) {
            return null;
        }
        List<Comic> list = new LinkedList<>();
        for (Node node : body.list(".main-list > div > div > div")) {
            String cid = node.attr("div:eq(1) > div:eq(0) > a", "href", "/", 2);
            String title = node.text("div:eq(1) > div:eq(0) > a");
            String cover = node.attr("div:eq(0) > a > img", "src");
            String update = node.text("div:eq(1) > div:eq(1) > dl:eq(3) > dd > font");
            String author = node.text("div:eq(1) > div:eq(1) > dl:eq(1) > dd > a");
            list.add(new Comic(source, cid, title, cover, update, author, null));
        }
        return list;
    }

    @Override
    protected Request buildIntoRequest(String cid) {
        String url = host + "/comic/" + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        Node body = MachiSoup.body(html);
        for (Node node : body.list("ul.list-body > li > a")) {
            String c_title = node.text();
            String c_path = node.attr("href", "/", 3);
            list.add(new Chapter(c_title, c_path));
        }

        String title = body.text("div.title-banner > div.book-title > h1", 0, -3);
        Node detail = body.select("div.book > div > div:eq(0)");
        String cover = detail.attr("div:eq(0) > a > img", "src");
        String update = detail.text("div:eq(1) > div > dl:eq(5) > dd > font", 0, 10);
        String author = detail.text("div:eq(1) > div > dl:eq(1) > dd > a");
        String intro = body.text("div.book-details > p:eq(1)");
        boolean status = "完结".equals(detail.text("div:eq(0) > div"));
        comic.setInfo(title, cover, update, intro, author, status);

        return list;
    }

    @Override
    protected Request buildBrowseRequest(String cid, String path) {
        String url = host + "/comic/" + cid + "/" + path;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<String> parseBrowse(String html) {
        String[] rs = MachiSoup.match("serverUrl = '(.*?)'[\\s\\S]*?eval(.*?)\\n;", html, 1, 2);
        if (rs != null) {
            try {
                String result = DecryptionUtils.evalDecrypt(rs[1]);
                String[] array = MachiSoup.match("pic_url='(.*?)';.*?tpf=(\\d+?);.*pages=(\\d+?);.*?pid=(.*?);.*?pic_extname='(.*?)';", result, 1, 2, 3, 4, 5);
                if (array != null) {
                    int tpf = Integer.parseInt(array[1]) + 1;
                    int pages = Integer.parseInt(array[2]);
                    String format = rs[0] + "/" + array[3] + "/" + array[0] + "/%0" + tpf + "d." + array[4];
                    List<String> list = new ArrayList<>(pages);
                    for (int i = 0; i != pages; ++i) {
                        list.add(String.format(Locale.CHINA, format, i + 1));
                    }
                    return list;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected Request buildCheckRequest(String cid) {
        String url = host + "/comic/" + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected String parseCheck(String html) {
        Node body = MachiSoup.body(html);
        return body.text("div.book > div > div:eq(0) > div:eq(1) > div > dl:eq(5) > dd > font", 0, 10);
    }

}
