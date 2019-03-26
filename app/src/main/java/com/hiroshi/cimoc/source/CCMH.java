package com.hiroshi.cimoc.source;

import android.util.Pair;

import com.google.common.collect.Lists;
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
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by WinterWhisper on 2019/2/25.
 */
public class CCMH extends MangaParser {

    public static final int TYPE = 23;
    public static final String DEFAULT_TITLE = "CC漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public CCMH(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = "";
        if (page == 1) {
            url = "http://m.ccmh6.com/Search";

            RequestBody requestBodyPost = new FormBody.Builder()
                    .add("Key", keyword)
                    .build();

            return new Request.Builder()
                    .addHeader("Referer", "http://m.ccmh6.com/Search")
                    .addHeader("Origin", "http://m.ccmh6.com")
                    .addHeader("Host", "m.ccmh6.com")
                    .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/12.0 Mobile/15A372 Safari/604.1")
                    .url(url)
                    .post(requestBodyPost)
                    .build();

        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(".UpdateList > .itemBox")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(".itemTxt > a", 1);
                String title = node.text(".itemTxt > a");
                String cover = node.src(".itemImg > a > img");
                if (cover.startsWith("//")) cover = "https:" + cover;
                String update = node.text(".itemTxt > p.txtItme:eq(3)");
                String author = node.text(".itemTxt > p.txtItme:eq(1)");
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return StringUtils.format("http://m.ccmh6.com/manhua/%s/", cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.50mh.com", "manhua\\/(\\w+)", 1));
    }


    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://m.ccmh6.com/manhua/%s/", cid);
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/12.0 Mobile/15A372 Safari/604.1")
                .url(url)
                .build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String intro = body.text(".txtDesc");
        String title = body.text("#comicName");
        String cover = body.src("#Cover > img");
        if (cover.startsWith("//")) cover = "https:" + cover;
        String author = body.text(".Introduct_Sub > .sub_r > .txtItme:eq(0)");
        String update = body.text(".Introduct_Sub > .sub_r > .txtItme:eq(4)");
        boolean status = isFinish(body.text(".Introduct_Sub > .sub_r > .txtItme:eq(2) > a:eq(3)"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list(".chapter-warp > ul > li > a")) {
            String title = node.text();
            String path = StringUtils.split(node.href(), "/", 3);
            list.add(new Chapter(title, path));
        }

        return Lists.reverse(list);
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.ccmh6.com/manhua/%s/%s", cid, path);
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/12.0 Mobile/15A372 Safari/604.1")
                .url(url)
                .build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String arrayString = StringUtils.match("var chapterImages = \\[([\\s\\S]*?)\\];", html, 1);
        String imagePath = StringUtils.match("var chapterPath = ([\\s\\S]*?);", html, 1).replace("\"", "");

        if (arrayString != null) {
            try {
                String[] array = arrayString.split(",");
                for (int i = 0; i != array.length; ++i) {
                    String imageUrl;
                    if (array[i].startsWith("\"http")) {
                        imageUrl = array[i].replace("\"", "");
                    } else {
                        imageUrl = "https://res.manhuachi.com/" + imagePath + array[i].replace("\"", "");
                    }
                    list.add(new ImageUrl(i + 1, imageUrl, false));
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
        return new Node(html).text(".Introduct_Sub > .sub_r > .txtItme:eq(4)");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.ccmh6.com/");
    }

}
