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
 * Created by Hiroshi on 2016/7/28.
 * neew fix
 */
public class CCTuku extends MangaParser {

    public static final int TYPE = 3;
    public static final String DEFAULT_TITLE = "CC图库";

    public CCTuku(Source source) {
        init(source, new Category());
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if(page == 1) {
            String url = StringUtils.format("http://m.tuku.cc/search-%s/?language=1", keyword);
            return new Request.Builder().url(url).build();
        } else return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("ul.searchResultList > li")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit("a", 1);
                String title = node.text("p.title");
                String cover = node.src("a > img");
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "http://m.tuku.cc/comic/".concat(cid);
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.tuku.cc/comic/".concat(cid);
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .url(url)
                .build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.detailTop > div.content > div.info > p.title");
        String cover = body.src("div.detailTop > div.content > img");
        String update = body.textWithSubstring("#chapter > div > div > div.top > span", 0, 10);
        // FIXME 这里可能有多个作者 暂时先取第一个
        String author = body.text("div.detailTop > div.content > div.info > p:eq(1) > a");
        String intro = body.text("div.detailContent > p:eq(1)");
        // FIXME 手机版页面好像获取不到状态 电脑板页面太大不想用 暂时先固定为连载吧
        comic.setInfo(title, cover, update, intro, author, false);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int i=0;
        for (Node node : body.list("#chapter > div > div > ul > li > a")) {
            String title = node.text();
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    private String _cid, _path;

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.tuku.cc/comic/%s/%s/", cid, path);
        _cid = cid;
        _path = path;
        return new Request.Builder()
                .addHeader("Referer", StringUtils.format("http://m.tuku.cc/comic/%s/", cid))
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        // TODO 好像拿不到总页数 GG
        List<ImageUrl> list = new ArrayList<>();
//        Node body = new Node(html);
//        int page = Integer.parseInt(body.attr("#hdPageCount", "value"));//max pages unknow...
        int page = 10;
        for (int i = 1; i <= page; ++i) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            list.add(new ImageUrl(id, comicChapter, i, StringUtils.format("http://m.tuku.cc/comic/%s/%s/p%s/", _cid, _path, i), true));
        }
        return list;
    }

    public Request getLazyRequest(String url) {
        return new Request.Builder()
                .addHeader("Referer", StringUtils.format("http://m.tuku.cc/comic/%s/%s/", _cid, _path))
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .url(url).build();
    }

    @Override
    public String parseLazy(String html, String url) {
        Node body = new Node(html);
        String src = body.href("div.readForm > a");
        return src;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).textWithSubstring("div.book > div > div.row > div:eq(1) > div > dl:eq(5) > dd > font", 0, 10);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
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
                list.add(new Comic(TYPE, cid, title, cover, update, author));
            }
        }
        return list;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.tuku.cc");
    }

    private static class Category extends MangaCategory {

        @Override
        public String getFormat(String... args) {
            if (!"".equals(args[CATEGORY_SUBJECT])) {
                return StringUtils.format("http://m.tuku.cc/list/list_%s_%%d.htm", args[CATEGORY_SUBJECT]);
            } else if (!"".equals(args[CATEGORY_AREA])) {
                return StringUtils.format("http://m.tuku.cc/list/comic_%s_%%d.htm", args[CATEGORY_AREA]);
            } else if (!"".equals(args[CATEGORY_PROGRESS])) {
                return StringUtils.format("http://m.tuku.cc/%s/%%d", args[CATEGORY_PROGRESS]);
            } else {
                return "http://m.tuku.cc/newest/%d";
            }
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("魔幻", "1"));
            list.add(Pair.create("动作", "2"));
            list.add(Pair.create("热血", "5"));
            list.add(Pair.create("爱情", "4"));
            list.add(Pair.create("武侠", "15"));
            list.add(Pair.create("搞笑", "7"));
            list.add(Pair.create("校园", "20"));
            list.add(Pair.create("竞技", "3"));
            list.add(Pair.create("科幻", "11"));
            list.add(Pair.create("悬疑", "10"));
            list.add(Pair.create("拳皇", "12"));
            list.add(Pair.create("恐怖", "9"));
            list.add(Pair.create("美女", "19"));
            list.add(Pair.create("励志", "8"));
            list.add(Pair.create("历史", "22"));
            list.add(Pair.create("百合", "35"));
            list.add(Pair.create("猎奇", "39"));
            list.add(Pair.create("职场", "38"));
            list.add(Pair.create("短篇", "34"));
            list.add(Pair.create("美食", "31"));
            list.add(Pair.create("四格", "30"));
            list.add(Pair.create("同人", "18"));
            list.add(Pair.create("青年", "17"));
            list.add(Pair.create("游戏", "14"));
            list.add(Pair.create("街霸", "13"));
            list.add(Pair.create("萌系", "6"));
            list.add(Pair.create("机战", "43"));
            list.add(Pair.create("节操", "42"));
            list.add(Pair.create("伪娘", "41"));
            list.add(Pair.create("后宫", "40"));
            list.add(Pair.create("耽美", "16"));
            list.add(Pair.create("其它", "33"));
            list.add(Pair.create("轻小说", "21"));
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
            list.add(Pair.create("香港", "1"));
            list.add(Pair.create("日本", "2"));
            list.add(Pair.create("欧美", "5"));
            list.add(Pair.create("台湾", "4"));
            list.add(Pair.create("韩国", "15"));
            list.add(Pair.create("大陆", "7"));
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
            list.add(Pair.create("连载", "lianzai"));
            list.add(Pair.create("完结", "wanjie"));
            return list;
        }

    }

}
