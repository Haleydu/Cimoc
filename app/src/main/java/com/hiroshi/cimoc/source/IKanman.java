package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaCategory;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class IKanman extends MangaParser {

    public IKanman() {
        server = new String[]{
                "http://p.yogajx.com",
                "http://idx0.hamreus.com:8080",
                "http://ilt2.hamreus.com:8080"
        };
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("http://m.ikanman.com/s/%s.html?page=%d&ajax=1", keyword, page);
        return new Request.Builder().url(url).build();
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
                return new Comic(SourceManager.SOURCE_IKANMAN, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.ikanman.com/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.main-bar > h1");
        String cover = body.src("div.book-detail > div.cont-list > div.thumb > img");
        String update = body.text("div.book-detail > div.cont-list > dl:eq(2) > dd");
        String author = body.attr("div.book-detail > div.cont-list > dl:eq(3) > dd > a", "title");
        String intro = body.text("#bookIntro");
        boolean status = isFinish(body.text("div.book-detail > div.cont-list > div.thumb > i"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        String url = "http://m.ikanman.com/support/chapters.aspx?id=".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("div.chapter-list")) {
            List<Chapter> ulList = new LinkedList<>();
            for (Node ul : node.list("ul")) {
                List<Chapter> liList = new LinkedList<>();
                for (Node li : ul.list("li > a")) {
                    String title = li.attr("title");
                    String path = li.hrefWithSplit(2);
                    liList.add(new Chapter(title, path));
                }
                ulList.addAll(0, liList);
            }
            list.addAll(ulList);
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.ikanman.com/comic/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("decryptDES\\(\"(.*?)\"\\)", html, 1);
        if (str != null) {
            try {
                String cipherStr = str.substring(8);
                String keyStr = str.substring(0, 8);
                String packed = DecryptionUtils.desDecrypt(keyStr, cipherStr);
                String result = DecryptionUtils.evalDecrypt(packed.substring(4));

                String jsonString = result.substring(11, result.length() - 9);
                JSONArray array = new JSONObject(jsonString).getJSONArray("images");
                for (int i = 0; i != array.length(); ++i) {
                    list.add(new ImageUrl(i + 1, buildUrl(array.getString(i)), false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = StringUtils.format("http://m.ikanman.com/update/?ajax=1&page=%d", page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("li > a")) {
            String cid = node.hrefWithSplit(1);
            String title = node.text("h3");
            String cover = node.attr("div > img", "data-src");
            String update = node.text("dl:eq(5) > dd");
            String author = node.text("dl:eq(2) > dd");
            list.add(new Comic(SourceManager.SOURCE_IKANMAN, cid, title, cover, update, author));
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
    public Request getCategoryRequest(String path, int page) {
        String url = StringUtils.format("http://m.ikanman.com/list/%s/?page=%d&catid=0&ajax=1", path, page);
        return new Request.Builder().url(url).build();
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
            list.add(new Comic(SourceManager.SOURCE_IKANMAN, cid, title, cover, update, author));
        }
        return list;
    }

    public static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            return null;
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

    }

}
