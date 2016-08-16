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

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/8.
 */
public class U17 extends Manga {

    public U17() {
        super(SourceManager.SOURCE_U17, "http://www.u17.com");
    }

    @Override
    protected Request buildSearchRequest(String keyword, int page) {
        String url = "http://so.u17.com/all/" + keyword + "/m0_p" + page + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Comic> parseSearch(String html, int page) {
        MachiSoup.Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (MachiSoup.Node node : body.list(".comiclist > ul > li > div")) {
            String cid = node.attr("div:eq(1) > h3 > strong > a", "href", "/|\\.", 6);
            String title = node.attr("div:eq(1) > h3 > strong > a", "title");
            String cover = node.attr("div:eq(0) > a > img", "src");
            String update = node.text("div:eq(1) > h3 > span.fr", 7);
            String author = node.text("div:eq(1) > h3 > a[title]");
            String[] array = node.text("div:eq(1) > p.cf > i.fl").split("/");
            boolean status = "已完结".equals(array[array.length - 1].trim());
            list.add(new Comic(source, cid, title, cover, update, author, status));
        }
        return list;
    }

    @Override
    protected Request buildIntoRequest(String cid) {
        String url = host + "/comic/" + cid + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        MachiSoup.Node body = MachiSoup.body(html);
        for (MachiSoup.Node node : body.list("#chapter > li > a")) {
            String c_title = node.text().trim();
            String c_path = node.attr("href", "/|\\.", 6);
            list.add(0, new Chapter(c_title, c_path));
        }

        Node detail = body.select("div.comic_info");
        String title = body.text("div:eq(0) > h1").trim();
        String cover = detail.attr("div:eq(0) > div.coverBox > div.cover > a > img", "src");
        String author = detail.text("div:eq(1) > div > div.info > a:eq(0)");
        String intro = detail.text("div:eq(0) > div.info > #words").trim();
        boolean status = "已完结".equals(body.text("div.main > div.info > div.fl > span.eq(2)"));
        String update = body.text("div.main > div.chapterlist > div.chapterlist_box > div.bot > div:eq(0) > span", 7);
        comic.setInfo(title, cover, update, intro, author, status);
        return list;
    }

    @Override
    protected Request buildBrowseRequest(String cid, String path) {
        String url = host + "/chapter/" + path + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<String> parseBrowse(String html) {
        List<String> result = MachiSoup.matchAll("\"src\":\"(.*?)\"", html, 1);
        if (!result.isEmpty()) {
            try {
                List<String> list = new ArrayList<>(result.size());
                for (String str : result) {
                    list.add(DecryptionUtils.base64Decrypt(str));
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected Request buildCheckRequest(String cid) {
        String url = host + "/comic/" + cid + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected String parseCheck(String html) {
        MachiSoup.Node body = MachiSoup.body(html);
        return body.text("div.main > div.chapterlist > div.chapterlist_box > div.bot > div:eq(0) > span", 7);
    }

}
