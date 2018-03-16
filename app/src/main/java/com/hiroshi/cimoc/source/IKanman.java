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

    public static final int TYPE = 0;
    public static final String DEFAULT_TITLE = "看漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public IKanman(Source source) {
        init(source, new Category());
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("http://m.manhuagui.com/s/%s.html?page=%d&ajax=1", keyword, page);
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .url(url)
                .build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("li > a")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(1);
                String title = node.text("h3");
                String cover = node.attr("div > img", "data-src");
                String update = node.text("dl:eq(5) > dd");
                String author = node.text("dl:eq(2) > dd");
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://www.manhuagui.com/comic/".concat(cid);
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
                .url(url)
                .build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.book-title > h1");
        String cover = body.src("p.hcover > img");
        String update = body.text("div.chapter-bar > span.fr > span:eq(1)");
        String author = body.attr("ul.detail-list > li:eq(1) > span:eq(1) > a", "title");
        String intro = body.text("#intro-cut");
        boolean status = isFinish(body.text("div.chapter-bar > span.fr > span:eq(0)"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        String baseText = body.id("__VIEWSTATE").attr("value");
        if (!StringUtils.isEmpty(baseText)) {
            body = new Node(DecryptionUtils.LZ64Decrypt(baseText));
        }
        for (Node node : body.list("div.chapter-list")) {
            List<Node> uls = node.list("ul");
            Collections.reverse(uls);
            for (Node ul : uls) {
                for (Node li : ul.list("li > a")) {
                    String title = li.attr("title");
                    String path = li.hrefWithSplit(2);
                    list.add(new Chapter(title, path));
                }
            }
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.manhuagui.com/comic/%s/%s.html", cid, path);
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .url(url)
                .build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String packed = StringUtils.match("\\(function\\(p,a,c,k,e,d\\).*?0,\\{\\}\\)\\)", html, 0);
        if (packed != null) {
            try {
                String replaceable = StringUtils.split(packed, ",", -3);
                String fake = StringUtils.split(replaceable, "'", 1);
                String real = DecryptionUtils.LZ64Decrypt(fake);
                packed = packed.replace(replaceable, StringUtils.format("'%s'.split('|')", real));
                String result = DecryptionUtils.evalDecrypt(packed);

                String jsonString = result.substring(11, result.length() - 9);
                JSONObject object = new JSONObject(jsonString);
                String chapterId = object.getString("chapterId");
                String md5 = object.getJSONObject("sl").getString("md5");
                JSONArray array = object.getJSONArray("images");
                for (int i = 0; i != array.length(); ++i) {
                    String url = StringUtils.format("http://i.hamreus.com/%s?cid=%s&md5=%s", array.getString(i), chapterId, md5);
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

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            String path = args[CATEGORY_AREA].concat(" ").concat(args[CATEGORY_SUBJECT]).concat(" ").concat(args[CATEGORY_READER])
                    .concat(" ").concat(args[CATEGORY_YEAR]).concat(" ").concat(args[CATEGORY_PROGRESS]).trim();
            path = path.replaceAll("\\s+", "_");
            return StringUtils.format("http://www.manhuagui.com/list/%s/%s_p%%d.html", path, args[CATEGORY_ORDER]);
        }

        @Override
        public List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("热血", "rexue"));
            list.add(Pair.create("冒险", "maoxian"));
            list.add(Pair.create("魔幻", "mohuan"));
            list.add(Pair.create("神鬼", "shengui"));
            list.add(Pair.create("搞笑", "gaoxiao"));
            list.add(Pair.create("萌系", "mengxi"));
            list.add(Pair.create("爱情", "aiqing"));
            list.add(Pair.create("科幻", "kehuan"));
            list.add(Pair.create("魔法", "mofa"));
            list.add(Pair.create("格斗", "gedou"));
            list.add(Pair.create("武侠", "wuxia"));
            list.add(Pair.create("机战", "jizhan"));
            list.add(Pair.create("战争", "zhanzheng"));
            list.add(Pair.create("竞技", "jingji"));
            list.add(Pair.create("体育", "tiyu"));
            list.add(Pair.create("校园", "xiaoyuan"));
            list.add(Pair.create("生活", "shenghuo"));
            list.add(Pair.create("励志", "lizhi"));
            list.add(Pair.create("历史", "lishi"));
            list.add(Pair.create("伪娘", "weiniang"));
            list.add(Pair.create("宅男", "zhainan"));
            list.add(Pair.create("腐女", "funv"));
            list.add(Pair.create("耽美", "danmei"));
            list.add(Pair.create("百合", "baihe"));
            list.add(Pair.create("后宫", "hougong"));
            list.add(Pair.create("治愈", "zhiyu"));
            list.add(Pair.create("美食", "meishi"));
            list.add(Pair.create("推理", "tuili"));
            list.add(Pair.create("悬疑", "xuanyi"));
            list.add(Pair.create("恐怖", "kongbu"));
            list.add(Pair.create("四格", "sige"));
            list.add(Pair.create("职场", "zhichang"));
            list.add(Pair.create("侦探", "zhentan"));
            list.add(Pair.create("社会", "shehui"));
            list.add(Pair.create("音乐", "yinyue"));
            list.add(Pair.create("舞蹈", "wudao"));
            list.add(Pair.create("杂志", "zazhi"));
            list.add(Pair.create("黑道", "heidao"));
            return list;
        }

        @Override
        public boolean hasArea() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getArea() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("日本", "japan"));
            list.add(Pair.create("港台", "hongkong"));
            list.add(Pair.create("其它", "other"));
            list.add(Pair.create("欧美", "europe"));
            list.add(Pair.create("内地", "china"));
            list.add(Pair.create("韩国", "korea"));
            return list;
        }

        @Override
        public boolean hasReader() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getReader() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("少女", "shaonv"));
            list.add(Pair.create("少年", "shaonian"));
            list.add(Pair.create("青年", "qingnian"));
            list.add(Pair.create("儿童", "ertong"));
            list.add(Pair.create("通用", "tongyong"));
            return list;
        }

        @Override
        public boolean hasYear() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getYear() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("2017年", "2017"));
            list.add(Pair.create("2016年", "2016"));
            list.add(Pair.create("2015年", "2015"));
            list.add(Pair.create("2014年", "2014"));
            list.add(Pair.create("2013年", "2013"));
            list.add(Pair.create("2012年", "2012"));
            list.add(Pair.create("2011年", "2011"));
            list.add(Pair.create("2010年", "2010"));
            list.add(Pair.create("00年代", "200x"));
            list.add(Pair.create("90年代", "199x"));
            list.add(Pair.create("80年代", "198x"));
            list.add(Pair.create("更早", "197x"));
            return list;
        }

        @Override
        public boolean hasProgress() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getProgress() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("连载", "lianzai"));
            list.add(Pair.create("完结", "wanjie"));
            return list;
        }

        @Override
        protected boolean hasOrder() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("更新", "update"));
            list.add(Pair.create("发布", "index"));
            list.add(Pair.create("人气", "view"));
            list.add(Pair.create("评分", "rate"));
            return list;
        }

    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.ikanman.com",
                "User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile");
    }

}
