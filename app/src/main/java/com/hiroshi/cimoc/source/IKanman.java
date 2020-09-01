package com.hiroshi.cimoc.source;

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
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class IKanman extends MangaParser {

    // TODO 实测联通4G网络无法使用看漫画

    public static final int TYPE = 0;
    public static final String DEFAULT_TITLE = "漫画柜";

    private String referer = "";

    public IKanman(Source source) {
        init(source, null);
//        init(source, new Category());
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("https://www.manhuagui.com/s/%s_p%d.html", keyword, page);
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 4.1.1; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166  Safari/535.19")
                .url(url)
                .build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("li.cf")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit("a.bcover", 1);
                String title = node.text(".book-detail > dl > dt > a");
                String cover = node.attr("a.bcover > img", "src");
//                String update = node.text("dl:eq(5) > dd");
//                String author = node.text("dl:eq(2) > dd");
                return new Comic(TYPE, cid, title, cover, "", "");
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "https://tw.manhuagui.com/comic/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.manhuagui.com"));
        filter.add(new UrlFilter("tw.manhuagui.com"));
        filter.add(new UrlFilter("m.manhuagui.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://tw.manhuagui.com/comic/".concat(cid).concat("/");
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 4.1.1; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166  Safari/535.19")
                .url(url)
                .build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.book-title > h1");
        String cover = body.src("p.hcover > img");
        String update = body.text("div.chapter-bar > span.fr > span:eq(1)");
        String author = body.attr("ul.detail-list > li:eq(1) > span:eq(1) > a", "title");
        String intro = body.text("#intro-cut");
        boolean status = isFinish(body.text("div.chapter-bar > span.fr > span:eq(0)"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        String baseText = body.id("__VIEWSTATE").attr("value");
        if (!StringUtils.isEmpty(baseText)) {
            body = new Node(DecryptionUtils.LZ64Decrypt(baseText));
        }
        int i=0;
        for (Node node : body.list("div.chapter-list")) {
            List<Node> uls = node.list("ul");
            Collections.reverse(uls);
            for (Node ul : uls) {
                for (Node li : ul.list("li > a")) {
                    String title = li.attr("title");
                    String path = li.hrefWithSplit(2);
                    list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
                }
            }
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://tw.manhuagui.com/comic/%s/%s.html", cid, path);
        referer = url;
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 4.1.1; Nexus 7 Build/JRO03D) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166  Safari/535.19")
//            .addHeader("Referer", StringUtils.format("https://m.manhuagui.com/comic/%s/%s.html", cid, path))
                .url(url)
                .build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        String packed = StringUtils.match("\\(function\\(p,a,c,k,e,d\\).*?0,\\{\\}\\)\\)", html, 0);
        if (packed != null) {
            try {
                String replaceable = StringUtils.split(packed, ",", -3);
                String fake = StringUtils.split(replaceable, "'", 1);
                String real = DecryptionUtils.LZ64Decrypt(fake);
                packed = packed.replace(replaceable, StringUtils.format("'%s'.split('|')", real));
                String result = DecryptionUtils.evalDecrypt(packed);

                String jsonString = result.substring(12, result.length() - 12);
                JSONObject object = new JSONObject(jsonString);
                String chapterId = object.getString("cid");
                String path = object.getString("path");
//                String md5 = object.getJSONObject("sl").getString("md5");
                String e = object.getJSONObject("sl").getString("e");
                String m = object.getJSONObject("sl").getString("m");
                JSONArray array = object.getJSONArray("files");
                for (int i = 0; i != array.length(); ++i) {
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    String url = StringUtils.format("https://i.hamreus.com%s%s?e=%s&m=%s", path, array.getString(i), e, m);
                    list.add(new ImageUrl(id, comicChapter, i + 1, url, false));
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
        return new Node(html).text("div.chapter-bar > span.fr > span:eq(1)");
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new ArrayList<>();
        Node body = new Node(html);
        for (Node node : body.list("#AspNetPager1 > span.current")) {
            try {
                if (Integer.parseInt(node.text()) < page) {
                    return list;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        for (Node node : body.list("#contList > li")) {
            String cid = node.hrefWithSplit("a", 1);
            String title = node.attr("a", "title");
            String cover = node.src("a > img");
            if (StringUtils.isEmpty(cover)) {
                cover = node.attr("a > img", "data-src");
            }
            String update = node.textWithSubstring("span.updateon", 4, 14);
            list.add(new Comic(TYPE, cid, title, cover, update, null));
        }
        return list;
    }

    @Override
    public Headers getHeader() {
//        return Headers.of("Referer", "https://tw.manhuagui.com/comic/30449/408812.html",
        return Headers.of("Referer", referer,
                "User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36");
    }

//    private static class Category extends MangaCategory {
//
//        @Override
//        public boolean isComposite() {
//            return true;
//        }
//
//        @Override
//        public String getFormat(String... args) {
//            String path = args[CATEGORY_AREA].concat(" ").concat(args[CATEGORY_SUBJECT]).concat(" ").concat(args[CATEGORY_READER])
//                .concat(" ").concat(args[CATEGORY_YEAR]).concat(" ").concat(args[CATEGORY_PROGRESS]).trim();
//            path = path.replaceAll("\\s+", "_");
//            return StringUtils.format("https://www.manhuagui.com/list/%s/%s_p%%d.html", path, args[CATEGORY_ORDER]);
//        }
//
//        @Override
//        public List<Pair<String, String>> getSubject() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
//            list.add(Pair.create("热血", "rexue"));
//            list.add(Pair.create("冒险", "maoxian"));
//            list.add(Pair.create("魔幻", "mohuan"));
//            list.add(Pair.create("神鬼", "shengui"));
//            list.add(Pair.create("搞笑", "gaoxiao"));
//            list.add(Pair.create("萌系", "mengxi"));
//            list.add(Pair.create("爱情", "aiqing"));
//            list.add(Pair.create("科幻", "kehuan"));
//            list.add(Pair.create("魔法", "mofa"));
//            list.add(Pair.create("格斗", "gedou"));
//            list.add(Pair.create("武侠", "wuxia"));
//            list.add(Pair.create("机战", "jizhan"));
//            list.add(Pair.create("战争", "zhanzheng"));
//            list.add(Pair.create("竞技", "jingji"));
//            list.add(Pair.create("体育", "tiyu"));
//            list.add(Pair.create("校园", "xiaoyuan"));
//            list.add(Pair.create("生活", "shenghuo"));
//            list.add(Pair.create("励志", "lizhi"));
//            list.add(Pair.create("历史", "lishi"));
//            list.add(Pair.create("伪娘", "weiniang"));
//            list.add(Pair.create("宅男", "zhainan"));
//            list.add(Pair.create("腐女", "funv"));
//            list.add(Pair.create("耽美", "danmei"));
//            list.add(Pair.create("百合", "baihe"));
//            list.add(Pair.create("后宫", "hougong"));
//            list.add(Pair.create("治愈", "zhiyu"));
//            list.add(Pair.create("美食", "meishi"));
//            list.add(Pair.create("推理", "tuili"));
//            list.add(Pair.create("悬疑", "xuanyi"));
//            list.add(Pair.create("恐怖", "kongbu"));
//            list.add(Pair.create("四格", "sige"));
//            list.add(Pair.create("职场", "zhichang"));
//            list.add(Pair.create("侦探", "zhentan"));
//            list.add(Pair.create("社会", "shehui"));
//            list.add(Pair.create("音乐", "yinyue"));
//            list.add(Pair.create("舞蹈", "wudao"));
//            list.add(Pair.create("杂志", "zazhi"));
//            list.add(Pair.create("黑道", "heidao"));
//            return list;
//        }
//
//        @Override
//        public boolean hasArea() {
//            return true;
//        }
//
//        @Override
//        public List<Pair<String, String>> getArea() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
//            list.add(Pair.create("日本", "japan"));
//            list.add(Pair.create("港台", "hongkong"));
//            list.add(Pair.create("其它", "other"));
//            list.add(Pair.create("欧美", "europe"));
//            list.add(Pair.create("内地", "china"));
//            list.add(Pair.create("韩国", "korea"));
//            return list;
//        }
//
//        @Override
//        public boolean hasReader() {
//            return true;
//        }
//
//        @Override
//        public List<Pair<String, String>> getReader() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
//            list.add(Pair.create("少女", "shaonv"));
//            list.add(Pair.create("少年", "shaonian"));
//            list.add(Pair.create("青年", "qingnian"));
//            list.add(Pair.create("儿童", "ertong"));
//            list.add(Pair.create("通用", "tongyong"));
//            return list;
//        }
//
//        @Override
//        public boolean hasYear() {
//            return true;
//        }
//
//        @Override
//        public List<Pair<String, String>> getYear() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
//            list.add(Pair.create("2017年", "2017"));
//            list.add(Pair.create("2016年", "2016"));
//            list.add(Pair.create("2015年", "2015"));
//            list.add(Pair.create("2014年", "2014"));
//            list.add(Pair.create("2013年", "2013"));
//            list.add(Pair.create("2012年", "2012"));
//            list.add(Pair.create("2011年", "2011"));
//            list.add(Pair.create("2010年", "2010"));
//            list.add(Pair.create("00年代", "200x"));
//            list.add(Pair.create("90年代", "199x"));
//            list.add(Pair.create("80年代", "198x"));
//            list.add(Pair.create("更早", "197x"));
//            return list;
//        }
//
//        @Override
//        public boolean hasProgress() {
//            return true;
//        }
//
//        @Override
//        public List<Pair<String, String>> getProgress() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
//            list.add(Pair.create("连载", "lianzai"));
//            list.add(Pair.create("完结", "wanjie"));
//            return list;
//        }
//
//        @Override
//        protected boolean hasOrder() {
//            return true;
//        }
//
//        @Override
//        protected List<Pair<String, String>> getOrder() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("更新", "update"));
//            list.add(Pair.create("发布", "index"));
//            list.add(Pair.create("人气", "view"));
//            list.add(Pair.create("评分", "rate"));
//            return list;
//        }
//
//    }

}
