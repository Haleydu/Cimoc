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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by PingZi on 2019/2/25.
 */

public class TuHao extends MangaParser {

    public static final int TYPE = 24;
    public static final String DEFAULT_TITLE = "土豪漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public TuHao(Source source) {
        init(source, new TuHao.Category());
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("https://m.tohomh123.com/action/Search?keyword=%s", keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#classList_1 > ul > li")) {
            @Override
            protected Comic parse(Node node) {

                String title = node.attr("a", "title");
                String urls = node.attr("a", "href");
                String cid = urls.substring(1, urls.length() - 1);
                String cover = node.attr("a > div > img", "src");
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://m.tohomh123.com/".concat(cid) + "/";
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String cover = body.src("div.coverForm > img");
        String intro = body.text("div.detailContent > p");
        String title = body.text("div.detailForm > div > div > h1");

        String update = "";
        String author = "";
        List<Node> upDateAndAuth = body.list("div.detailForm > div > div > p");

        if (upDateAndAuth.size() == 5) {
            update = upDateAndAuth.get(3).text().substring(5).trim();
            author = upDateAndAuth.get(2).text().substring(3).trim();
        } else {
            update = upDateAndAuth.get(2).text().substring(5).trim();
            author = upDateAndAuth.get(1).text().substring(3).trim();
        }

        // 连载状态
        boolean status = isFinish("连载");
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#chapterList_1 > ul > li > a")) {
            String title = node.text();
            String path = node.hrefWithSplit(1);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://m.tohomh123.com/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();

        String str = StringUtils.match("var pl = \'(.*?)\'", html, 1);
        int pagNum = Integer.parseInt(StringUtils.match("var pcount=(.*?);", html, 1));

        if (str != null) {
            try {
                for (int i = 0; i < pagNum; ++i) {
                    String lastNum = i + "";
                    if (lastNum.length() != 4) {
                        //需要补0的个数
                        int num = 4 - lastNum.length();
                        for (int j = 0; j < num; j++) {
                            lastNum = "0" + lastNum;
                        }
                    }
                    String url = str.substring(0, str.length() - 8) + lastNum + ".jpg";
//                  https://mh2.wan1979.com/upload/jiemoren/1989998/0000.jpg
                    list.add(new ImageUrl(i + 1, url, false));
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
        Node body = new Node(html);

        String update = "";
        List<Node> upDateAndAuth = body.list("div.detailForm > div > div > p");

        if (upDateAndAuth.size() == 5) {
            update = upDateAndAuth.get(3).text().substring(5).trim();
        } else {
            update = upDateAndAuth.get(2).text().substring(5).trim();
        }
        return update;
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#classList_1 > ul > li")) {
            String title = node.attr("a", "title");
            String urls = node.attr("a", "href");
            String cid = urls.substring(1, urls.length() - 1);
            String cover = node.attr("a > div > img", "src");
            list.add(new Comic(TYPE, cid, title, cover, null, null));
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
            return StringUtils.format("https://m.tohomh123.com/act/?act=list&page=%%d&catid=%s&ajax=1&order=%s",
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
        return Headers.of("Referer", "https://www.tohomh123.com");
    }

}
