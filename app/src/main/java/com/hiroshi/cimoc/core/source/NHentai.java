package com.hiroshi.cimoc.core.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.MachiSoup;

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
        MachiSoup.Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (MachiSoup.Node node : body.list("#content > div.index-container > div > a")) {
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
        MachiSoup.Node doc = MachiSoup.body(html);
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
    protected String[] parseBrowse(String html) {
        MachiSoup.Node body = MachiSoup.body(html);
        List<MachiSoup.Node> list = body.list("#thumbnail-container > div > a > img");
        String[] array = new String[list.size()];
        for (int i = 0; i != list.size(); ++i) {
            String url = "https:" + list.get(i).attr("data-src");
            array[i] = url.replace("t.jpg", ".jpg");
        }
        return array;
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
