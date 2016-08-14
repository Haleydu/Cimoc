package com.hiroshi.cimoc.core.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/9.
 */
public class Wnacg extends Manga {

    public Wnacg() {
        super(SourceManager.SOURCE_WNACG, "http://www.wnacg.com");
    }

    @Override
    protected Request buildSearchRequest(String keyword, int page) {
        String url = host + "/albums-index-page-" + page + "-sname-" + keyword + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Comic> parseSearch(String html, int page) {
        Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (MachiSoup.Node node : body.list("#bodywrap > div.grid > div > ul > li")) {
            String cid = node.attr("div.info > div.title > a", "href", "-|\\.", 3);
            String title = node.text("div.info > div.title > a");
            String cover = node.attr("div.pic_box > a > img", "data-original");
            String update = node.text("div.info > div.info_col").trim();
            update = MachiSoup.match("\\d{4}-\\d{2}-\\d{2}", update, 0);
            list.add(new Comic(source, cid, title, cover, update, null, true));
        }
        return list;
    }

    @Override
    protected Request buildIntoRequest(String cid) {
        String url = host + "/photos-index-aid-" + cid + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        MachiSoup.Node body = MachiSoup.body(html);
        String length = body.text("#bodywrap > div > div.uwconn > label:eq(1)", 3, -2);
        int size = Integer.parseInt(length) % 12 == 0 ? Integer.parseInt(length) / 12 : Integer.parseInt(length) / 12 + 1;
        for (int i = 1; i <= size; ++i) {
            list.add(0, new Chapter("Ch" + i, String.valueOf(i)));
        }

        String title = body.text("#bodywrap > h2");
        String intro = body.text("#bodywrap > div > div.uwconn > p", 3);
        String author = body.text("#bodywrap > div > div.uwuinfo > p");
        String cover = body.attr("#bodywrap > div > div.uwthumb > img", "data-original");
        comic.setInfo(title, cover, "", intro, author, true);

        return list;
    }

    @Override
    protected Request buildBrowseRequest(String cid, String path) {
        String url = host + "/photos-index-page-" + path + "-aid-" + cid + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected String[] parseBrowse(String html) {
        MachiSoup.Node body = MachiSoup.body(html);
        List<MachiSoup.Node> list = body.list("#bodywrap > div.grid > div > ul > li > div.pic_box > a");
        String[] array = new String[list.size()];
        for (int i = 0; i != array.length; ++i) {
            String url = host + list.get(i).attr("href");
            Request request = new Request.Builder().url(url).build();
            String result = execute(request);
            if (result != null) {
                MachiSoup.Node node = MachiSoup.body(result);
                array[i] = node.attr("#picarea", "src");
            } else {
                array[i] = null;
            }
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
