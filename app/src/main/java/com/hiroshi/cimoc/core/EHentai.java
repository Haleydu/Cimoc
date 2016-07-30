package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/26.
 */
public class EHentai extends Manga {

    public EHentai() {
        super(Kami.SOURCE_EHENTAI, "http://lofi.e-hentai.org");
    }

    @Override
    protected Request buildSearchRequest(String keyword, int page) {
        String url = host + "?f_search=" + keyword + "&page=" + (page - 1);
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Comic> parseSearch(String html) {
        Node body = MachiSoup.body(html);
        List<Node> nodes = body.list("#ig > div > table > tbody > tr");
        List<Comic> list = new LinkedList<>();
        for (Node node : nodes) {
            String cid = node.attr("td:eq(1) > table > tbody > tr:eq(0) > td > a", "href");
            cid = cid.substring(host.length() + 3, cid.length() - 1);
            String title = node.text("td:eq(1) > table > tbody > tr:eq(0) > td > a");
            String cover = node.attr("td:eq(0) > a > img", "src");
            String update = node.text("td:eq(1) > table > tbody > tr:eq(1) > td:eq(1)", 0, 10);
            String author = node.text("td:eq(1) > table > tbody > tr:eq(1) > td:eq(1)", 20);
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
    protected String[] parseBrowse(String html) {
        Node body = MachiSoup.body(html);
        List<Node> list = body.list("#gh > div > a");
        String[] array = new String[list.size()];
        for (int i = 0; i != list.size(); ++i) {
            String url = list.get(i).attr("href");
            String result = execute(url);
            if (result != null) {
                Node node = MachiSoup.body(result);
                array[i] = node.attr("#sm", "src");
            } else {
                array[i] = null;
            }
        }
        return array;
    }

}
