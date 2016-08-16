package com.hiroshi.cimoc.core.source;

import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/26.
 */
public class HHAAZZ extends Manga {

    public HHAAZZ() {
        super(SourceManager.SOURCE_HHAAZZ, "http://hhaazz.com");
    }

    @Override
    protected Request buildSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = host + "/comicsearch/s.aspx?s=" + keyword;
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    protected List<Comic> parseSearch(String html, int page) {
        Node body = MachiSoup.body(html);
        List<Node> nodes = body.list(".se-list > li");
        List<Comic> list = new LinkedList<>();
        for (Node node : nodes) {
            String cid = node.attr("a:eq(0)", "href", "/", 4);
            String title = node.text("a:eq(0) > div > h3");
            String cover = node.attr("a:eq(0) > img", "src");
            String update = node.text("a:eq(0) > div > p:eq(4) > span", 0, 10);
            String author = node.text("a:eq(0) > div > p:eq(1)");
            boolean status = "完结".equals(node.text("a:eq(1) > span:eq(1)"));
            list.add(new Comic(source, cid, title, cover, update, author, status));
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
        List<Node> nodes = body.list("#sort_div_p > a");
        for (Node node : nodes) {
            String c_title = node.attr("title");
            String c_path = node.attr("href").substring(host.length());
            list.add(new Chapter(c_title, c_path));
        }

        Node detail = body.select(".main > div > div.pic");
        String title = detail.text("div:eq(1) > h3");
        String cover = detail.attr("img:eq(0)", "src");
        String update = detail.text("div:eq(1) > p:eq(5)", 5);
        String author = detail.text("div:eq(1) > p:eq(1)", 3);
        String intro = body.text("#detail_block > div > p");
        boolean status = detail.text("div:eq(1) > p:eq(4)").contains("完结");
        comic.setInfo(title, cover, update, intro, author, status);

        return list;
    }

    @Override
    protected Request buildBrowseRequest(String cid, String path) {
        String url = host + path;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<String> parseBrowse(String html) {
        String[] str = MachiSoup.match("sFiles=\"(.*?)\";var sPath=\"(\\d+)\"", html, 1, 2);
        if (str != null) {
            String[] result = unsuan(str[0]);
            String domain = String.format(Locale.CHINA, "http://x8.1112223333.com:9393/dm%02d", Integer.parseInt(str[1]));
            List<String> list = new ArrayList<>(result.length);
            for (int i = 0; i != result.length; ++i) {
                list.add(domain + result[i]);
            }
            return list;
        }
        return null;
    }

    private static String[] unsuan(String str) {
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
    protected Request buildCheckRequest(String cid) {
        String url = host + "/comic/" + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected String parseCheck(String html) {
        Node body = MachiSoup.body(html);
        return body.text(".main > div > div.pic > div:eq(1) > p:eq(5)", 5);
    }

}
