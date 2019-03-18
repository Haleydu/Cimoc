package com.hiroshi.cimoc.source;

import android.util.Pair;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaCategory;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Reborn on 2019/03/18.
 */

public class ChuiXue extends MangaParser {

    public static final int TYPE = 52;
    public static final String DEFAULT_TITLE = "吹雪漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public ChuiXue(Source source) {
        init(source, new ChuiXue.Category());
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        // http://www.chuixue.net/e/search/?searchget=1&show=title,player,playadmin,pinyin&keyboard=%B0%D9%C1%B6%B3%C9%C9%F1
        // http://m.chuixue.net/e/search/?searchget=1&tbname=movie&tempid=1&show=title,keyboard&keyboard=%B0%D9%C1%B6%B3%C9%C9%F1
        String url = StringUtils.format("http://m.chuixue.net/e/search/?searchget=1&tbname=movie&tempid=1&show=title,keyboard&keyboard=%s",
                URLEncoder.encode(keyword, "GB2312"));
//        return new Request.Builder().url(url).build();

        //解决重复加载列表问题
        if (url.equals(ResultActivity.searchUrls.get(TYPE))) {
            return null;
        } else {
            if (ResultActivity.searchUrls.get(TYPE) == null) {
                ResultActivity.searchUrls.append(TYPE, url);
            } else {
                ResultActivity.searchUrls.setValueAt(TYPE, url);
            }
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("div.cont-list > ul > li")) {//li > a
            @Override
            protected Comic parse(Node node) {
                String cid = node.getChild("a").hrefWithSplit(1);//node.hrefWithSplit(1);
                String title = node.text("a > h3");
                String cover = node.attr("a > div > img", "data-src");
                String update = node.text("a > dl:eq(5) > dd");
                String author = node.text("a > dl:eq(2) > dd");
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.chuixue.net/manhua/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.main-bar > h1");
        String cover = body.src("div.book-detail > div.cont-list > div.thumb > img");
        String update = body.text("div.book-detail > div.cont-list > dl:eq(4) > dd");
        String author = body.text("div.book-detail > div.cont-list > dl:eq(3) > dd");
        String intro = body.text("#bookIntro");
        boolean status = isFinish(body.text("div.book-detail > div.cont-list > div.thumb > i"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#chapterList > ul > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.chuixue.net/manhua/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        html = html.replace("\r\n", "").replace(" ", "").trim();
        String str = StringUtils.match("(?<=newArray\\(\\)\\;\\$\\(function\\(\\)\\{)(.*?)(?=;varpackedarr)", html, 1);
        if (str != null) {
            try {
                String[] array = str.split(";");
                for (int i = 0; i != array.length; ++i) {
                    list.add(new ImageUrl(i + 1, "http://2.csc1998.com/" + StringUtils.match("(?<=\")(.*?)(?=\")", array[i], 1), false));
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
        return new Node(html).text("div.book-detail > div.cont-list > dl:eq(2) > dd");
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("div.cont-list > ul > li")) {//li > a
            String cid = node.getChild("a").hrefWithSplit(1);//node.hrefWithSplit(1);
            String title = node.text("a > h3");
            String cover = node.attr("a > div > img", "data-src");
            String update = node.text("a > dl:eq(5) > dd");
            String author = node.text("a > dl:eq(2) > dd");
            list.add(new Comic(TYPE, cid, title, cover, update, author));
        }
        return list;
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            // http://m.chuixue.net/plus/mlist.php?page=%%d&classid=%%d&tempid=%%d&line=%%d&ajax=%%d
            // Top: http://m.chuixue.net/plus/mlist.php?page=1&classid=8&tempid=29&line=20&ajax=1 第二页
            // Up : http://m.chuixue.net/plus/mlist.php?page=1&classid=8&tempid=28&line=20&ajax=1 第二页
            // Up : http://m.chuixue.net/plus/mlist.php?page=2&classid=8&tempid=28&line=20&ajax=1 第三页
            if (args[0].equals("newchapter") || args[0].equals("top")) {
                return StringUtils.format("http://m.chuixue.net/%s.html",
                        args[CATEGORY_SUBJECT]);
            }
            return StringUtils.format("http://m.chuixue.net/%s/%s_%%d.html",
                    args[CATEGORY_SUBJECT], args[CATEGORY_ORDER]);
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
            list.add(Pair.create("最近更新", "newchapter"));
            list.add(Pair.create("漫画排行", "top"));
//            list.add(Pair.create("恋爱生活", "9"));
            return list;
        }

        @Override
        protected boolean hasOrder() {
            return false;
        }

        @Override
        protected List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("发布", "index"));
            list.add(Pair.create("更新", "update"));
            list.add(Pair.create("人气", "view"));
            return list;
        }

    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.chuixue.net");
    }

}
