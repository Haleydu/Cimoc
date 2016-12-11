package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.core.parser.NodeIterator;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/28.
 */
public class CCTuku extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("http://m.tuku.cc/comic/search?word=%s&page=%d", keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        int total = Integer.parseInt(StringUtils.match("\\d+", body.text("div.title-banner > div > h1"), 0));
        if (page > total) {
            return null;
        }
        return new NodeIterator(body.list("div.main-list > div > div > div")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit("div:eq(1) > div:eq(0) > a", 1);
                String title = node.text("div:eq(1) > div:eq(0) > a");
                String cover = node.src("div:eq(0) > a > img");
                String update = node.text("div:eq(1) > div:eq(1) > dl:eq(3) > dd > font");
                String author = node.text("div:eq(1) > div:eq(1) > dl:eq(1) > dd > a");
                return new Comic(SourceManager.SOURCE_CCTUKU, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.tuku.cc/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.textWithSubstring("div.title-banner > div.book-title > h1", 0, -3);
        String cover = body.src("div.book > div > div.row > div:eq(0) > a > img");
        String update = body.textWithSubstring("div.book > div > div.row > div:eq(1) > div > dl:eq(5) > dd > font", 0, 10);
        String author = body.text("div.book > div > div.row > div:eq(1) > div > dl:eq(1) > dd > a");
        String intro = body.text("div.book-details > p:eq(1)");
        boolean status = isFinish(body.text("div.book > div > div.row > div:eq(0) > div"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("ul.list-body > li > a")) {
            String title = node.text();
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.tuku.cc/comic/%s/%s", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String[] rs = StringUtils.match("serverUrl = '(.*?)'[\\s\\S]*?eval(.*?)\\n;", html, 1, 2);
        if (rs != null) {
            try {
                String result = DecryptionUtils.evalDecrypt(rs[1]);
                String[] array = StringUtils.match("pic_url='(.*?)';.*?tpf=(\\d+?);.*pages=(\\d+?);.*?pid=(.*?);.*?pic_extname='(.*?)';", result, 1, 2, 3, 4, 5);
                if (array != null) {
                    int tpf = Integer.parseInt(array[1]) + 1;
                    int pages = Integer.parseInt(array[2]);
                    String format = rs[0] + "/" + array[3] + "/" + array[0] + "/%0" + tpf + "d." + array[4];
                    for (int i = 0; i != pages; ++i) {
                        list.add(new ImageUrl(i + 1, StringUtils.format(format, i + 1), false));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = StringUtils.format("http://m.tuku.cc/newest/%d", page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        int total = Integer.parseInt(StringUtils.match("\\d+", body.text("div.title-banner > div > h1"), 0));
        if (page <= total) {
            for (Node node : body.list("div.main-list > div > div > div")) {
                String cid = node.hrefWithSplit("div:eq(1) > div:eq(0) > a", 1);
                String title = node.text("div:eq(1) > div:eq(0) > a");
                String cover = node.src("div:eq(0) > a > img");
                String update = node.text("div:eq(1) > div:eq(1) > dl:eq(3) > dd > font");
                String author = node.text("div:eq(1) > div:eq(1) > dl:eq(1) > dd > a");
                list.add(new Comic(SourceManager.SOURCE_CCTUKU, cid, title, cover, update, author));
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
        return new Node(html).textWithSubstring("div.book > div > div.row > div:eq(1) > div > dl:eq(5) > dd > font", 0, 10);
    }

    public List<Pair<String, String>> getCategoryList() {
        List<Pair<String, String>> list = new ArrayList<>();
        list.add(Pair.create("魔幻神话", "1"));
        list.add(Pair.create("动作格斗", "2"));
        list.add(Pair.create("少年热血", "5"));
        list.add(Pair.create("少女爱情", "4"));
        list.add(Pair.create("武侠漫画", "15"));
        list.add(Pair.create("轻松搞笑", "7"));
        list.add(Pair.create("校园青春", "20"));
        list.add(Pair.create("体育竞技", "3"));
        list.add(Pair.create("科幻未来", "11"));
        list.add(Pair.create("悬疑探案", "10"));
        list.add(Pair.create("拳皇漫画", "12"));
        list.add(Pair.create("恐怖鬼怪", "9"));
        list.add(Pair.create("惊险", "91"));
        list.add(Pair.create("美女漫画", "19"));
        list.add(Pair.create("励志冒险", "8"));
        list.add(Pair.create("历史漫画", "22"));
        list.add(Pair.create("百合漫画", "35"));
        list.add(Pair.create("爆笑喜剧", "101"));
        list.add(Pair.create("竞技体育", "99"));
        list.add(Pair.create("其它漫画", "98"));
        list.add(Pair.create("侦探推理", "97"));
        list.add(Pair.create("科幻魔幻", "96"));
        list.add(Pair.create("武侠格斗", "95"));
        list.add(Pair.create("青年", "94"));
        list.add(Pair.create("生活", "93"));
        list.add(Pair.create("复仇", "92"));
        list.add(Pair.create("治愈", "90"));
        list.add(Pair.create("后宫", "89"));
        list.add(Pair.create("战争", "88"));
        list.add(Pair.create("虐心", "87"));
        list.add(Pair.create("唯美", "86"));
        list.add(Pair.create("都市", "85"));
        list.add(Pair.create("恋爱", "84"));
        list.add(Pair.create("穿越", "83"));
        list.add(Pair.create("福利", "82"));
        list.add(Pair.create("猎奇", "39"));
        list.add(Pair.create("职场", "38"));
        list.add(Pair.create("短篇漫画", "34"));
        list.add(Pair.create("综合其它", "33"));
        list.add(Pair.create("江湖漫", "32"));
        list.add(Pair.create("厨艺美食", "31"));
        list.add(Pair.create("四格漫画", "30"));
        list.add(Pair.create("动漫游图集", "29"));
        list.add(Pair.create("动漫杂志", "24"));
        list.add(Pair.create("连环画", "23"));
        list.add(Pair.create("轻小说", "21"));
        list.add(Pair.create("同人漫画", "18"));
        list.add(Pair.create("青年漫画", "17"));
        list.add(Pair.create("游戏漫画", "14"));
        list.add(Pair.create("街霸漫画", "13"));
        list.add(Pair.create("萌", "6"));
        list.add(Pair.create("机战", "43"));
        list.add(Pair.create("节操", "42"));
        list.add(Pair.create("伪娘", "41"));
        list.add(Pair.create("后宫", "40"));
        list.add(Pair.create("耽美BL", "16"));
        return list;
    }

    @Override
    public Request getCategoryRequest(String id, int page) {
        String url = StringUtils.format("http://m.tuku.cc/list/comic_%s_%d.htm", id, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseCategory(String html) {
        return null;
    }
}
