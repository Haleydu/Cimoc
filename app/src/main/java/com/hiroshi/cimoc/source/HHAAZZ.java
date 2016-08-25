package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.utils.MachiSoup;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

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
    public List<Comic> parseSearch(String html, int page) {
        MachiSoup.Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (MachiSoup.Node node : body.list("ul.se-list > li")) {
            String cid = node.attr("a.pic", "href", "/", 4);
            String title = node.text("a.pic > div > h3");
            String cover = node.attr("a.pic > img", "src");
            String update = node.text("a.pic > div > p:eq(4) > span", 0, 10);
            String author = node.text("a.pic > div > p:eq(1)");
            boolean status = node.text("a.tool > span.h").contains("完结");
            list.add(new Comic(SourceManager.SOURCE_HHAAZZ, cid, title, cover, update, author, status));
        }
        return list;
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://hhaazz.com/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseInfo(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        MachiSoup.Node body = MachiSoup.body(html);
        for (MachiSoup.Node node : body.list("#sort_div_p > a")) {
            String c_title = node.attr("title");
            String c_path = node.attr("href").substring(17);
            list.add(new Chapter(c_title, c_path));
        }

        String title = body.text("div.main > div > div.pic > div.con > h3");
        String cover = body.attr("div.main > div > div.pic > img", "src");
        String update = body.text("div.main > div > div.pic > div.con > p:eq(5)", 5);
        String author = body.text("div.main > div > div.pic > div.con > p:eq(1)", 3);
        String intro = body.text("#detail_block > div > p");
        boolean status = body.text("div.main > div > div.pic > div.con > p:eq(4)").contains("完结");
        comic.setInfo(title, cover, update, intro, author, status);

        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = "http://hhaazz.com/".concat(path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String[] str = MachiSoup.match("sFiles=\"(.*?)\";var sPath=\"(\\d+)\"", html, 1, 2);
        if (str != null) {
            String[] result = unsuan(str[0]);
            String domain = String.format(Locale.getDefault(), "http://64.140.165.116:9393/dm%02d", Integer.parseInt(str[1]));
            for (int i = 0; i != result.length; ++i) {
                list.add(new ImageUrl(i + 1, domain + result[i], false));
            }
        }
        return list;
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
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return MachiSoup.body(html).text("div.main > div > div.pic > div:eq(1) > p:eq(5)", 5);
    }

}
