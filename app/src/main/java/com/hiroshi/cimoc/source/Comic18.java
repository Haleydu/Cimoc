package com.hiroshi.cimoc.source;

import com.google.common.collect.Lists;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.HttpUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Request;

public class Comic18 extends MangaParser {

    public static final int TYPE = 72;
    public static final String DEFAULT_TITLE = "禁漫天堂";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public Comic18(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        if (page != 1) return null;
        String url = StringUtils.format("https://18comic.vip/search/photos?search_query=%s&main_tag=0", keyword);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        Node body = new Node(html);
        return new NodeIterator(body.list("div#wrapper > div.container > div.row > div > div.row > div")) {
            @Override
            protected Comic parse(Node node) {
                final String cid = node.href("div.thumb-overlay > a");
                final String title = node.text("span.video-title");
                final String cover = node.attr("div.thumb-overlay > a > img", "data-original");
                final String update = node.text("div.video-views");
                final String author = "";
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "https://18comic.vip" + cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("18comic.vip"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://18comic.vip" + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String intro = body.text("div.col-lg-7 > div > div:eq(6)");
        String title = body.text("div.panel-heading > div");
        String cover = body.attr("img#album_photo_cover","src");
        String author = body.text("div.col-lg-7 > div > div:eq(3) > span > a");
        String update = body.text("div.episode > ul > a > li > span:eq(1)");
        boolean status = isFinish(body.text("div.col-lg-7 > div > div:eq(2) > span > a(1)"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("div.episode > ul > a")) {
            String titlearray[] = (node.text("li").split("\\s+"));
            String path = node.href();
            list.add(new Chapter(titlearray[0], path));
        }
        return list;
    }

    private String imgpath = "";
    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = "https://18comic.vip"+path;
        imgpath = path;
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new ArrayList<>();
        try {
            int i = 0;
            for (Node node : new Node(html).list("img.lazy_img")) {
                String img1 = node.attr("img","src");
                String img2 = node.attr("img","data-original");
                String reg[] = imgpath.split("\\/");
                if (img1.contains(reg[2])){
                    list.add(new ImageUrl(i++, img1, false));
                }else if (img2.contains(reg[2])){
                    list.add(new ImageUrl(i++, img2, false));
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }finally {
            return list;
        }
    }


    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "https://18comic.vip/");
    }
}
