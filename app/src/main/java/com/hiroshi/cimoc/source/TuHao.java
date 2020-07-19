package com.hiroshi.cimoc.source;

import android.util.Log;

import com.google.common.collect.Lists;
import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.LogUtil;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Request;

import static com.hiroshi.cimoc.core.Manga.getResponseBody;

/**
 * Created by ZhiWen on 2019/02/25.
 */

public class TuHao extends MangaParser {

    public static final int TYPE = 24;
    public static final String DEFAULT_TITLE = "土豪漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public TuHao(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            //https://m.tuhaomh.com/e/search/index.php?searchget=1&tempid=1&tbname=book&show=title,writer&keyboard=%s
            url = StringUtils.format("https://m.tuhaomh.com/e/search/index.php?searchget=1&tempid=1&tbname=book&show=title,writer&keyboard=%s",
                    //URLEncoder.encode(keyword, "GB2312"));
                    keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public String getUrl(String cid) {
        return "https://m.tuhaomh.com/"+ cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.tuhaomh.com"));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("div.bd > ul.comic-sort > li")) {
            @Override
            protected Comic parse(Node node) {
                String title = node.attr("div.comic-item > div.thumbnail >a","title");
                title = title.replace("<font color='red'>", "");
                title = title.replace("</font>", "");
                String urls = node.attr("a", "href");
                String cid = urls.substring(1, urls.length());
                String cover = node.attr("div.comic-item > div.thumbnail > a > img", "data-src");
                String update =  getHtml(cid).text("time#updateTime");
                return new Comic(TYPE, cid, title, cover, update, null);
            }
        };
    }

    private Node getHtml(String cid) {
        Node bodyupdate = null;
        try {
            String url = "https://m.tuhaomh.com/" + cid;
            String imhtml = getResponseBody(App.getHttpClient(), new Request.Builder().url(url).build());
            bodyupdate = new Node(imhtml);
        } catch (Manga.NetworkErrorException e) {
            e.printStackTrace();
        }
        return bodyupdate;
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://m.tuhaomh.com/"+ cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String cover = body.attr("div.mk-detail > div.comic-info > div.cover-bg > img.thumbnail","data-src");
        String intro = body.text("p.content");
        String title = body.text("h1.name");

        String update = body.text("time#updateTime");
        String author = body.text("span.author");

        // 连载状态
        boolean status = isFinish("连载");
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("div.mk-chapterlist-box > div.bd > ul.chapterlist > li.item")) {
            String title = node.text("a.chapterBtn");
            String path = node.attr("a","href");
            list.add(new Chapter(title, path));
        }
        return Lists.reverse(list);
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = "https://m.tuhaomh.com"+ path;
        imgurl = url;
        return new Request.Builder().url(url).build();
    }
    private String imgurl = "";

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        Node body = new Node(html);
        int total = Integer.parseInt(body.text("span.total-page"));
        String img = body.attr("img#comic_pic","src");
        list.add(new ImageUrl(0, img, false));

        if (imgurl != null) {
            try {
                for (int i = 1; i < total; i++) {
                    String imghtml = "-"+i+".html";
                    String targetUrl = imgurl.replace(".html", imghtml);
                    String imhtml = getResponseBody(App.getHttpClient(), new Request.Builder().url(targetUrl).build());
                    Node body1 = new Node(imhtml);
                    String img1 = body1.attr("img#comic_pic","src");
                    list.add(new ImageUrl(i, img1, false));
                }
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
        // 这里表示的是更新时间
        return new Node(html).text("time#updateTime");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "https://m.tuhaomh.com");
    }

}
