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
 * Created by Hiroshi on 2016/8/14.
 */
public class NHentai extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = String.format(Locale.getDefault(), "https://nhentai.net/search/?q=%s&page=%d", keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseSearch(String html, int page) {
        Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (Node node : body.list("#content > div.index-container > div > a")) {
            String cid = node.attr("href", "/", 2);
            String title = node.text("div.caption");
            String author = MachiSoup.match("\\[(.*?)\\]", title, 1);
            title = title.replaceFirst("\\[.*?\\]\\s+", "");
            String cover = "https:".concat(node.attr("img", "src"));
            list.add(new Comic(SourceManager.SOURCE_NHENTAI, cid, title, cover, "", author, true));
        }
        return list;
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = String.format(Locale.getDefault(), "https://nhentai.net/g/%s", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseInfo(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        Node doc = MachiSoup.body(html);
        list.add(new Chapter("全一话", ""));

        String title = doc.text("#info > h1");
        String intro = doc.text("#info > h2");
        String author = doc.text("#tags > div > span > a[href^=/artist/]");
        String cover = "https:" + doc.attr("#cover > a > img", "src");
        comic.setInfo(title, cover, "", intro, author, true);

        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        return getInfoRequest(cid);
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        Node body = MachiSoup.body(html);
        List<ImageUrl> list = new LinkedList<>();
        int count = 0;
        for (Node node : body.list("#thumbnail-container > div > a > img")) {
            String url = "https:".concat(node.attr("data-src"));
            list.add(new ImageUrl(++count, url.replace("t.jpg", ".jpg"), false));
        }
        return list;
    }

}
