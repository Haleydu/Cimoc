package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/21.
 */
public class Chuiyao extends Manga {

    public Chuiyao() {
        super(Kami.SOURCE_CHUIYAO, "http://www.chuiyao.com/");
    }

    @Override
    protected String parseSearchUrl(String keyword, int page) {
        return host + "/search/?key=" + keyword + "&page=" + page;
    }

    @Override
    protected List<Comic> parseSearch(String html) {
        Document doc = Jsoup.parse(html);
        Elements items = doc.select("#dmList > ul > li");
        List<Comic> list = new LinkedList<>();
        for (Element item : items) {
            String path = item.select("dl > dt > a").first().attr("href");
            String title = item.select("dl > dt > a").first().attr("title");
            String image = item.select("p > a > img").first().attr("src");
            String update = item.select("dl > dd > p:eq(0) > span").first().text();
            boolean status = "完结".equals(item.select("dl > dd > p:eq(1) > span").first().text());
            list.add(build(path, title, image, update, null, null, status));
        }
        return list;
    }

    @Override
    protected String parseIntoUrl(String path) {
        return host + path;
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        Document doc = Jsoup.parse(html);
        Elements items = doc.select("#play_0 > ul > li > a");
        for (Element item : items) {
            String c_title = item.attr("title");
            String c_path = item.attr("href").replace(host, "");
            list.add(new Chapter(c_title, c_path));
        }

        Element detail = doc.getElementById("intro_l");
        String title = detail.select(".title > h1").first().text();
        String image = detail.select(".cover > img").first().attr("src");
        String update = detail.select(".info > p:eq(0) > span").first().text();
        String author = detail.select(".info > p:eq(1)").first().text().replace("原著作者：", "").trim();
        String intro = doc.select("#intro10 > p").first().text();
        fill(comic, title, image, update, author, intro, null);
        return list;
    }

    @Override
    protected String parseBrowseUrl(String path) {
        return host + path;
    }

    @Override
    protected String[] parseBrowse(String html) {
        return new String[0];
    }
}
