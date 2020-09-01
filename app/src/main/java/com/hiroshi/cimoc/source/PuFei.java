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
import com.hiroshi.cimoc.parser.UrlFilter;
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
 * Created by FEILONG on 2017/12/21.
 * fix by Haleydu on 2020/8/30
 */

public class PuFei extends MangaParser {

    public static final int TYPE = 50;
    public static final String DEFAULT_TITLE = "扑飞漫画";

    public PuFei(Source source) {
        init(source, new PuFei.Category());
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("http://m.pufei8.com/e/search/?searchget=1&tbname=mh&show=title,player,playadmin,bieming,pinyin,playadmin&tempid=4&keyboard=%s",
                    URLEncoder.encode(keyword, "GB2312"));
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#detail > li")) {
            @Override
            protected Comic parse(Node node) {
                Node node_a = node.list("a").get(0);
                String cid = node_a.hrefWithSplit(1);
                String title = node_a.text("h3");
                String cover = node_a.attr("div > img", "data-src");
                String author = node_a.text("dl > dd");
                String update = node.text("dl:eq(4) > dd");
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "http://m.pufei8.com/manhua/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.pufei.net"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.pufei8.com/manhua/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.main-bar > h1");
        String cover = body.src("div.book-detail > div.cont-list > div.thumb > img");
        String update = body.text("div.book-detail > div.cont-list > dl:eq(2) > dd");
        String author = body.text("div.book-detail > div.cont-list > dl:eq(3) > dd");
        String intro = body.text("#bookIntro");
        boolean status = isFinish(body.text("div.book-detail > div.cont-list > div.thumb > i"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("#chapterList2 > ul > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);

            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.pufei8.com/manhua/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("cp=\"(.*?)\"", html, 1);
        if (str != null) {
            try {
                str = DecryptionUtils.evalDecrypt(DecryptionUtils.base64Decrypt(str));
                String[] array = str.split(",");
                for (int i = 0; i != array.length; ++i) {
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    list.add(new ImageUrl(id, comicChapter, i + 1, "http://res.img.youzipi.net/" + array[i], false));
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
            String update = node.text("dl:eq(4) > dd");
            String author = node.text("a > dl:eq(2) > dd");
            list.add(new Comic(TYPE, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.pufei8.com");
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            return StringUtils.format("http://m.pufei.com/act/?act=list&page=%%d&catid=%s&ajax=1&order=%s",
                    args[CATEGORY_SUBJECT], args[CATEGORY_ORDER]);
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
            list.add(Pair.create("最近更新", "manhua/update"));
            list.add(Pair.create("漫画排行", "manhua/paihang"));
            list.add(Pair.create("少年热血", "shaonianrexue"));
            list.add(Pair.create("武侠格斗", "wuxiagedou"));
            list.add(Pair.create("科幻魔幻", "kehuan"));
            list.add(Pair.create("竞技体育", "jingjitiyu"));
            list.add(Pair.create("搞笑喜剧", "gaoxiaoxiju"));
            list.add(Pair.create("侦探推理", "zhentantuili"));
            list.add(Pair.create("恐怖灵异", "kongbulingyi"));
            list.add(Pair.create("少女爱情", "shaonvaiqing"));
            list.add(Pair.create("耽美BL", "danmeirensheng"));
//            list.add(Pair.create("恋爱生活", "9"));
            return list;
        }

        @Override
        protected boolean hasOrder() {
            return true;
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

}
