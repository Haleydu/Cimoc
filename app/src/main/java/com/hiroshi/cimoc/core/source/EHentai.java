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
 * Created by Hiroshi on 2016/7/26.
 */
public class EHentai extends Manga {

    public EHentai() {
        super(SourceManager.SOURCE_EHENTAI, "http://lofi.e-hentai.org");
    }

    @Override
    protected Request buildSearchRequest(String keyword, int page) {
        String url = host + "?f_search=" + keyword + "&page=" + (page - 1);
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Comic> parseSearch(String html, int page) {
        Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (Node node : body.list("#ig > div > table > tbody > tr")) {
            String cid = node.attr("td:eq(1) > table > tbody > tr:eq(0) > td > a", "href");
            cid = cid.substring(host.length() + 3, cid.length() - 1);
            String title = node.text("td:eq(1) > table > tbody > tr:eq(0) > td > a");
            String cover = node.attr("td:eq(0) > a > img", "src");
            String update = node.text("td:eq(1) > table > tbody > tr:eq(1) > td:eq(1)", 0, 10);
            String author = MachiSoup.match("\\[(.*?)\\]", title, 1);
            title = title.replaceFirst("\\[.*?\\]\\s+", "");
            list.add(new Comic(source, cid, title, cover, update, author, true));
        }
        return list;
    }

    @Override
    protected Request buildIntoRequest(String cid) {
        String url = "http://g.e-hentai.org/g/" + cid;
        return new Request.Builder().url(url).header("Cookie", "nw=1").build();
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        Node doc = MachiSoup.body(html);
        String length = doc.text("#gdd > table > tbody > tr:eq(5) > td:eq(1)", " ", 0);
        int size = Integer.parseInt(length) % 8 == 0 ? Integer.parseInt(length) / 8 : Integer.parseInt(length) / 8 + 1;
        for (int i = 0; i != size; ++i) {
            list.add(0, new Chapter("Ch" + i, "/" + i));
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
    protected Request buildBrowseRequest(String cid, String path) {
        String url = host + "/g/" + cid  + path;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<String> parseBrowse(String html) {
        Node body = MachiSoup.body(html);
        List<Node> nodes = body.list("#gh > div > a");
        List<String> list = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            String url = node.attr("href");
            Request request = new Request.Builder().url(url).build();
            String result = execute(request);
            if (result != null) {
                list.add(MachiSoup.body(result).attr("#sm", "src"));
            } else {
                list.add(null);
            }
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
