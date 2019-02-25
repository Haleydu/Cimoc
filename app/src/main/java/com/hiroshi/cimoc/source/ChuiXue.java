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
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by PingZi on 2019/2/25.
 */

public class ChuiXue extends MangaParser {

    public static final int TYPE = 69;
    public static final String DEFAULT_TITLE = "吹雪漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public ChuiXue(Source source) {
        init(source, new ChuiXue.Category());
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("http://www.chuixue.net/e/search/?searchget=1&show=title,player,playadmin,pinyin&keyboard=%s",
                    URLEncoder.encode(keyword, "GB2312"));
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#dmList> ul > li")) {
            @Override
            protected Comic parse(Node node) {
                Node node_cover = node.list("p > a").get(0);
                String cid = node_cover.hrefWithSplit(1);
                String title = node_cover.attr("img", "alt");
                String cover = node_cover.attr("img", "_src");
                String update = node.text("dl > dd > p:eq(0) > span");
                return new Comic(TYPE, cid, title, cover, update, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://www.chuixue.net/manhua/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.intro_l > div.title > h1");
        String cover = body.src("div.intro_l > div.info_cover > p.cover > img");
        String update = body.text("div.intro_l > div.info > p:eq(0) > span");
        String author = body.text("div.intro_l > div.info > p:eq(1)").substring(5).trim();
        String intro = body.text("#intro");
        boolean status = isFinish(body.text("div.intro_l > div.info > p:eq(2)"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#play_0 > ul > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://www.chuixue.net/manhua/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("photosr\\[1\\](.*?)(\n|var)", html, 1);
        if (str != null) {
            try {
                String[] array = str.split(";");
                for (int i = 0; i != array.length; ++i) {
                    String s_full = array[i].trim();
                    int index = s_full.indexOf("=");
                    String s = s_full.substring(index + 2, s_full.length() - 1);
                    list.add(new ImageUrl(i + 1, "http://2.csc1998.com/" + s, false));
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
        return new Node(html).text("div.intro_l > div.info > p:eq(0) > span");
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#dmList> ul > li")) {
            Node node_cover = node.list("p > a").get(0);
            String cid = node_cover.hrefWithSplit(1);
            String title = node_cover.attr("img", "alt");
            String cover = node_cover.attr("img", "_src");
            String update = node.text("dl > dd > p:eq(0) > span");
            list.add(new Comic(TYPE, cid, title, cover, update, null));
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
            return StringUtils.format("http://www.chuixue.com/act/?act=list&page=%%d&catid=%s&ajax=1&order=%s",
                    args[CATEGORY_SUBJECT], args[CATEGORY_ORDER]);
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("最近更新", "0"));
            list.add(Pair.create("少年热血", "1"));
            list.add(Pair.create("武侠格斗", "2"));
            list.add(Pair.create("科幻魔幻", "3"));
            list.add(Pair.create("竞技体育", "4"));
            list.add(Pair.create("爆笑喜剧", "5"));
            list.add(Pair.create("侦探推理", "6"));
            list.add(Pair.create("恐怖灵异", "7"));
            list.add(Pair.create("少女爱情", "8"));
            list.add(Pair.create("恋爱生活", "9"));
            return list;
        }

        @Override
        protected boolean hasOrder() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("更新", "3"));
            list.add(Pair.create("发布", "1"));
            list.add(Pair.create("人气", "2"));
            return list;
        }

    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://www.chuixue.net");
    }

}
