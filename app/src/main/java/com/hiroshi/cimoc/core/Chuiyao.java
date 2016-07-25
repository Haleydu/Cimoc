package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Document doc = Jsoup.parse(html);
        Elements items = doc.body().select("li > a");
        List<Comic> list = new LinkedList<>();
        for (Element item : items) {
            String cid = item.attr("href").split("/")[2];
            String title = item.select("h3:eq(1)").first().text();

            String cover = item.select("div:eq(0) > img").first().attr("data-src");
            String update = item.select("dl:eq(5) > dd").first().text();
            String author = item.select("dl:eq(2) > dd").first().text();
            boolean status = "完结".equals(item.select("div:eq(0) > i").first().text().trim());
            list.add(build(cid, title, cover, update, author, null, status));
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
        Document doc = Jsoup.parse(html);
        Elements items = doc.select("#chapterList > ul > li > a");
        for (Element item : items) {
            String c_title = item.attr("title");
            String c_path = item.attr("href").split("/|\\.")[7];
            list.add(new Chapter(c_title, c_path));
        }

        String title = doc.select(".main-bar > h1").first().text();
        Element detail = doc.getElementsByClass("book-detail").first();
        String cover = detail.select(".cont-list > div:eq(0) > img").first().attr("src");
        String update = detail.select(".cont-list > dl:eq(2) > dd").first().text();
        String author = detail.select(".cont-list > dl:eq(3) > dd").first().text();
        Element node = detail.getElementById("bookIntro");
        String intro = node.select("p:eq(0)").isEmpty() ? node.text() : node.select("p:eq(0)").first().text();
        boolean status = "完结".equals(detail.select(".cont-list > div:eq(0) > i").first().text());

        comic.setIntro(intro);
        comic.setTitle(title);
        comic.setCover(cover);
        comic.setAuthor(author);
        comic.setStatus(status);
        comic.setUpdate(update);

        return list;
    }

    @Override
    protected String parseBrowseUrl(String cid, String path) {
        return host + "/manhua/" + cid + "/" + path + ".html";
    }

    @Override
    protected String[] parseBrowse(String html) {
        Pattern pattern = Pattern.compile("parseJSON\\('(\\[.*?\\])'");
        Matcher matcher = pattern.matcher(html);
        if (matcher.find()) {
            try {
                JSONArray array = new JSONArray(matcher.group(1));
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
