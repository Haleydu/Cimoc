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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/26.
 */
public class HHAAZZ extends MangaParser {

    public static final int TYPE = 2;
    public static final String DEFAULT_TITLE = "手机汗汗";

    private static final String[] servers = {
            "http://x8.1112223333.com/dm01/",
            "http://x8.1112223333.com/dm02/",
            "http://x8.1112223333.com/dm03/",
            "http://x8.1112223333.com/dm04/",
            "http://x8.1112223333.com/dm05/",
            "http://x8.1112223333.com/dm06/",
            "http://x8.1112223333.com/dm07/",
            "http://x8.1112223333.com/dm08/",
            "http://x8.1112223333.com/dm09/",
            "http://x8.1112223333.com/dm10/",
            "http://x8.1112223333.com/dm11/",
            "http://x8.1112223333.com/dm12/"
    };

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public HHAAZZ(Source source) {
        init(source, new Category());
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = "http://hhaass.com/comicsearch/s.aspx?s=".concat(keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("ul.se-list > li")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit("a.pic", 1);
                String title = node.text("a.pic > div > h3");
                String cover = node.src("a.pic > img");
                String update = node.textWithSubstring("a.pic > div > p:eq(4) > span", 0, 10);
                String author = node.text("a.pic > div > p:eq(1)");
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://hhaass.com/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.main > div > div.pic > div.con > h3");
        String cover = body.src("div.main > div > div.pic > img");
        String update = body.textWithSubstring("div.main > div > div.pic > div.con > p:eq(5)", 5);
        String author = body.textWithSubstring("div.main > div > div.pic > div.con > p:eq(1)", 3);
        String intro = body.text("#detail_block > div > p");
        boolean status = isFinish(body.text("div.main > div > div.pic > div.con > p:eq(4)"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#sort_div_p > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSubString(17);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = "http://hhaass.com/".concat(path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String[] str = StringUtils.match("sFiles=\"(.*?)\";var sPath=\"(\\d+)\"", html, 1, 2);
        if (str != null) {
            String[] result = unsuan(str[0]);
            for (int i = 0; i != result.length; ++i) {
                list.add(new ImageUrl(i + 1, servers[Integer.parseInt(str[1]) - 1].concat(result[i]), false));
            }
        }
        return list;
    }

    private String[] unsuan(String str) {
        int num = str.length() - str.charAt(str.length() - 1) + 'a';
        String code = str.substring(num - 13, num - 3);
        String cut = str.substring(num - 3, num - 2);
        str = str.substring(0, num - 13);
        for (int i = 0; i < 10; ++i) {
            str = str.replace(code.charAt(i), (char) ('0' + i));
        }
        StringBuilder builder = new StringBuilder();
        String[] array = str.split(cut);
        for (int i = 0; i != array.length; ++i) {
            builder.append((char) Integer.parseInt(array[i]));
        }
        return builder.toString().split("\\|");
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).textWithSubstring("div.main > div > div.pic > div.con > p:eq(5)", 5);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("li.clearfix > a.pic")) {
            String cid = node.hrefWithSplit(1);
            String title = node.text("div.con > h3");
            String cover = node.src("img");
            String update = node.textWithSubstring("div.con > p > span", 0, 10);
            String author = node.text("div.con > p:eq(1)");
            list.add(new Comic(TYPE, cid, title, cover, update, author));
        }
        return list;
    }

    private static class Category extends MangaCategory {

        @Override
        public String getFormat(String... args) {
            if (!"".equals(args[CATEGORY_SUBJECT])) {
                return StringUtils.format("http://hhaass.com/lists/%s/%%d", args[CATEGORY_SUBJECT]);
            } else if (!"".equals(args[CATEGORY_AREA])) {
                return StringUtils.format("http://hhaass.com/lists/%s/%%d", args[CATEGORY_AREA]);
            } else if (!"".equals(args[CATEGORY_READER])) {
                return StringUtils.format("http://hhaass.com/duzhequn/%s/%%d", args[CATEGORY_PROGRESS]);
            } else if (!"".equals(args[CATEGORY_PROGRESS])) {
                return StringUtils.format("http://hhaass.com/lianwan/%s/%%d", args[CATEGORY_PROGRESS]);
            } else {
                return "http://hhaass.com/dfcomiclist_%d.htm";
            }
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("萌系", "1"));
            list.add(Pair.create("搞笑", "2"));
            list.add(Pair.create("格斗", "3"));
            list.add(Pair.create("科幻", "4"));
            list.add(Pair.create("剧情", "5"));
            list.add(Pair.create("侦探", "6"));
            list.add(Pair.create("竞技", "7"));
            list.add(Pair.create("魔法", "8"));
            list.add(Pair.create("神鬼", "9"));
            list.add(Pair.create("校园", "10"));
            list.add(Pair.create("惊栗", "11"));
            list.add(Pair.create("厨艺", "12"));
            list.add(Pair.create("伪娘", "13"));
            list.add(Pair.create("图片", "14"));
            list.add(Pair.create("冒险", "15"));
            list.add(Pair.create("耽美", "21"));
            list.add(Pair.create("经典", "22"));
            list.add(Pair.create("亲情", "25"));
            return list;
        }

        @Override
        protected boolean hasArea() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getArea() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("大陆", "19"));
            list.add(Pair.create("香港", "20"));
            list.add(Pair.create("欧美", "23"));
            list.add(Pair.create("日文", "24"));
            return list;
        }

        @Override
        protected boolean hasReader() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getReader() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("少年", "1"));
            list.add(Pair.create("少女", "2"));
            list.add(Pair.create("青年", "3"));
            return list;
        }

        @Override
        protected boolean hasProgress() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getProgress() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("连载", "1"));
            list.add(Pair.create("完结", "2"));
            return list;
        }

    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://hhaass.com/");
    }

}
