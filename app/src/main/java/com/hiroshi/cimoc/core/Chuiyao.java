package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import org.json.JSONArray;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/21.
 */
public class Chuiyao extends Manga {

    public Chuiyao() {
        super(Kami.SOURCE_CHUIYAO, "http://m.chuiyao.com");
    }

    @Override
    protected String parseSearchUrl(String keyword, int page) {
        return host + "/search/?act=search&key=" + keyword + "&page=" + page;
    }

    @Override
    protected List<Comic> parseSearch(String html) {
        Node doc = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (Node item : doc.list("li > a")) {
            String cid = item.attr("href", "/", 2);
            String title = item.text("h3:eq(1)");
            String cover = item.attr("div:eq(0) > img", "data-src");
            String update = item.text("dl:eq(5) > dd");
            String author = item.text("dl:eq(2) > dd");
            boolean status = "完结".equals(item.text("div:eq(0) > i").trim());
            list.add(new Comic(source, cid, title, cover, update, author, status));
        }
        return list;
    }

    @Override
    protected String parseIntoUrl(String cid) {
        return host + "/manhua/" + cid;
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        Node doc = MachiSoup.body(html);
        for (Node item : doc.list("#chapterList > ul > li > a")) {
            String c_title = item.attr("title");
            String c_path = item.attr("href", "/|\\.", 7);
            list.add(new Chapter(c_title, c_path));
        }

        String title = doc.text(".main-bar > h1");
        Node detail = doc.select(".book-detail");
        String cover = detail.attr("div:eq(0) > div:eq(0) > img", "src");
        String update = detail.text("div:eq(0) > dl:eq(2) > dd");
        String author = detail.text("div:eq(0) > dl:eq(3) > dd");
        Node temp = detail.id("bookIntro");
        String intro = temp.exist("p:eq(0)") ? temp.text() : temp.text("p:eq(0)");
        boolean status = "完结".equals(detail.text(".cont-list > div:eq(0) > i"));
        comic.setInfo(title, cover, update, intro, author, status);

        return list;
    }

    @Override
    protected String parseBrowseUrl(String cid, String path) {
        return host + "/manhua/" + cid + "/" + path + ".html";
    }

    @Override
    protected String[] parseBrowse(String html) {
        String jsonString = MachiSoup.match("parseJSON\\('(\\[.*?\\])'", html, 1);
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                String[] images = new String[array.length()];
                for (int i = 0; i != array.length(); ++i) {
                    images[i] = array.getString(i);
                }
                return images;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
