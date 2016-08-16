package com.hiroshi.cimoc.core.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/14.
 */
public class NHentai extends Manga {

    public NHentai() {
        super(SourceManager.SOURCE_NHENTAI, "https://nhentai.net");
    }

    @Override
    protected Request buildSearchRequest(String keyword, int page) {
        String url = host + "/search/?q=" + keyword + "&page=" + page;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Comic> parseSearch(String html, int page) {
        Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (Node node : body.list("#content > div.index-container > div > a")) {
            String cid = node.attr("href", "/", 2);
            String title = node.text("div.caption");
            String author = MachiSoup.match("\\[(.*?)\\]", title, 1);
            title = title.replaceFirst("\\[.*?\\]\\s+", "");
            String cover = "https:" + node.attr("img", "src");
            list.add(new Comic(source, cid, title, cover, "", author, true));
        }
        return list;
    }

    @Override
    protected Request buildIntoRequest(String cid) {
        String url = host + "/g/" + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
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
    protected Request buildBrowseRequest(String cid, String path) {
        String url = host + "/g/" + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<String> parseBrowse(String html) {
        Node body = MachiSoup.body(html);
        List<Node> nodes = body.list("#thumbnail-container > div > a > img");
        List<String> list = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            String url = "https:" + node.attr("data-src");
            list.add(url.replace("t.jpg", ".jpg"));
        }
        return list;
    }

    @Override
    protected Request buildCheckRequest(String cid) {
        return null;
    }

    @Override
    protected String parseCheck(String html) {
        return null;
    }

}
