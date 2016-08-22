package com.hiroshi.cimoc.core.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.source.base.MangaParser;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/26.
 */
public class EHentai extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = String.format(Locale.CHINA, "http://g.e-hentai.org/?f_search=%s&page=%d", keyword, (page - 1));
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseSearch(String html, int page) {
        Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (Node node : body.list("table.itg > tbody > tr[class^=gtr]")) {
            String cid = node.attr("td:eq(2) > div > div:eq(2) > a", "href");
            cid = cid.substring(24, cid.length() - 1);
            String title = node.text("td:eq(2) > div > div:eq(2) > a");
            String cover = node.attr("td:eq(2) > div > div:eq(0) > img", "src");
            if (cover == null) {
                String temp = node.text("td:eq(2) > div > div:eq(0)", 14).split("~", 2)[0];
                cover = "http://ehgt.org/" + temp;
            }
            String update = node.text("td:eq(1)", 0, 10);
            String author = MachiSoup.match("\\[(.*?)\\]", title, 1);
            title = title.replaceFirst("\\[.*?\\]\\s+", "");
            list.add(new Comic(SourceManager.SOURCE_EHENTAI, cid, title, cover, update, author, true));
        }
        return list;
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = String.format(Locale.CHINA, "http://g.e-hentai.org/g/%s", cid);
        return new Request.Builder().url(url).header("Cookie", "nw=1").build();
    }

    @Override
    public List<Chapter> parseInfo(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        Node doc = MachiSoup.body(html);
        String length = doc.text("#gdd > table > tbody > tr:eq(5) > td:eq(1)", " ", 0);
        int size = Integer.parseInt(length) % 40 == 0 ? Integer.parseInt(length) / 40 : Integer.parseInt(length) / 40 + 1;
        for (int i = 0; i != size; ++i) {
            list.add(0, new Chapter("Ch" + i, String.valueOf(i)));
        }

        String update = doc.text("#gdd > table > tbody > tr:eq(0) > td:eq(1)", 0, 10);
        String title = doc.text("#gn");
        String intro = doc.text("#gj");
        String author = doc.text("#taglist > table > tbody > tr > td:eq(1) > div > a[id^=ta_artist]");
        String cover = doc.attr("#gd1 > img", "src");
        comic.setInfo(title, cover, update, intro, author, true);

        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = String.format(Locale.CHINA, "http://g.e-hentai.org/g/%s/?p=%s", cid, path);
        return new Request.Builder().url(url).header("Cookie", "nw=1").build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        Node body = MachiSoup.body(html);
        for (Node node : body.list("#gh > div > a")) {
            list.add(new ImageUrl(node.attr("href"), true));
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder().url(url).build();
    }

    @Override
    public String parseLazy(String html) {
        return MachiSoup.body(html).attr("#sm", "src");
    }

}
