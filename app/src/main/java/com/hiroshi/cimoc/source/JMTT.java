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

public class JMTT extends MangaParser {

    public static final int TYPE = 72;
    public static final String DEFAULT_TITLE = "禁漫天堂";
    public static final String baseUrl = "https://18comic1.one/"; //https://cm365.xyz/7MJX9t

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    public JMTT(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        if (page != 1) return null;
        String url = StringUtils.format(baseUrl + "/search/photos?search_query=%s&main_tag=0", keyword);
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
        return baseUrl + cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(baseUrl));
        filter.add(new UrlFilter("https://18comic1.one/"));
        filter.add(new UrlFilter("https://18comic2.one/"));
        filter.add(new UrlFilter("https://18comic.vip"));
        filter.add(new UrlFilter("18comic.org"));
        filter.add(new UrlFilter("https://cm365.xyz/7MJX9t"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseUrl + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        try {
            Node body = new Node(html);
            String intro = body.text("#intro-block > div:eq(0)");
            String title = body.text("div.panel-heading > div");
            String cover = body.attr("img.lazy_img.img-responsive","src").trim();
            String author = body.text("#intro-block > div:eq(4) > span");
            String update = body.attr("#album_photo_cover > div:eq(1) > div:eq(3)","content");
            boolean status = isFinish(body.text("#intro-block > div:eq(2) > span"));
            comic.setInfo(title, cover, update, intro, author, status);
        }catch (Exception e){
            e.printStackTrace();
        }
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int i=0;
        String startTitle = body.text(".col.btn.btn-primary.dropdown-toggle.reading").trim();
        String startPath = body.href(".col.btn.btn-primary.dropdown-toggle.reading");
        list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, startTitle, startPath));
        for (Node node : body.list("#episode-block > div > div.episode > ul > a")) {
            String title = node.text("li").trim();
            String path = node.href();
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return Lists.reverse(list);
    }

    private String imgpath = "";
    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = baseUrl+path;
        imgpath = path;
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new ArrayList<>();
        try {
            int i = 0;
            for (Node node : new Node(html).list("img.lazy_img")) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);

                String img1 = node.attr("img","src");
                String img2 = node.attr("img","data-original");
                String reg[] = imgpath.split("\\/");
                if (img1.contains(reg[2])){
                    list.add(new ImageUrl(id, comicChapter, ++i, img1, false));
                }else if (img2.contains(reg[2])){
                    list.add(new ImageUrl(id, comicChapter, ++i, img2, false));
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }


    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).attr("#album_photo_cover > div:eq(1) > div:eq(3)","content");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", baseUrl);
    }
}
