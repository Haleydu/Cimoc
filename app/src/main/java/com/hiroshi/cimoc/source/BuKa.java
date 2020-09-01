package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.JsonIterator;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by FEILONG on 2017/12/21.
 */

public class BuKa extends MangaParser {

    public static final int TYPE = 52;
    public static final String DEFAULT_TITLE = "布卡漫画";

    public BuKa(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "http://m.buka.cn/search/ajax_search";
        RequestBody data = new FormBody.Builder()
                .add("key", keyword)
                .add("start", String.valueOf(15 * (page - 1)))
                .add("count", "15")
                .build();//key=%E4%B8%8D%E5%AE%9C%E5%AB%81&start=0&count=15
        return new Request.Builder()
                .url(url)
                .post(data)
                .build();
    }

    @Override
    public SearchIterator getSearchIterator(String json, int page) {
        try {
            JSONObject object = new JSONObject(json);
            JSONObject dataObject = object.getJSONObject("datas");
            JSONArray dataArray = dataObject.getJSONArray("items");
            return new JsonIterator(dataArray) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        String cid = object.getString("mid");
                        String title = object.getString("name");
                        String cover = object.getString("logo");
                        String author = object.getString("author");
                        return new Comic(TYPE, cid, title, cover, null, author);
                    } catch (Exception ex) {
                        return null;
                    }
                }
            };
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public String getUrl(String cid) {
        return "http://m.buka.cn/m/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.buka.cn"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.buka.cn/m/".concat(cid);
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("p.mangadir-glass-name");
        String cover = body.src(".mangadir-glass-img > img");
        String update = body.text("span.top-title-right");
        String author = body.text(".mangadir-glass-author");
        String intro = body.text("span.description_intro");
        boolean status = isFinish("连载中");//todo: fix here
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

//    @Override
//    public Request getChapterRequest(String html, String cid){
//        String url = "https://m.ac.qq.com/comic/chapterList/id/".concat(cid);
//        return new Request.Builder()
//            .url(url)
//            .build();
//    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("div.chapter-center > a")) {
            String title = node.text();
            String path = node.href().split("/")[3];
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.buka.cn/read/%s/%s", cid, path);
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .url(url)
                .build();
    }

    @Override
    public List<ImageUrl> parseImages(String html,Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        Matcher m = Pattern.compile("<img class=\"lazy\" data-original=\"(http.*?jpg)\" />").matcher(html);
        if (m.find()) {
            try {
                int i = 0;
                do {
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    list.add(new ImageUrl(id,comicChapter,++i, StringUtils.match("http.*jpg", m.group(0), 0), false));
                } while (m.find());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.book-detail > div.cont-list > dl:eq(2) > dd");
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("li > a")) {
            String cid = node.hrefWithSplit(1);
            String title = node.text("h3");
            String cover = node.attr("div > img", "data-src");
            String update = node.text("dl:eq(5) > dd");
            String author = node.text("dl:eq(2) > dd");
            list.add(new Comic(TYPE, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.buka.cn");
    }

}
