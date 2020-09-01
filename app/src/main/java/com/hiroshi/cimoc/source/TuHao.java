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
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by ZhiWen on 2019/02/25.
 */

public class TuHao extends MangaParser {

    public static final int TYPE = 24;
    public static final String DEFAULT_TITLE = "土豪漫画";
    private static final String website = "tuhao456.com";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public TuHao(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("https://%s/sort/?key=%s", website, keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public String getUrl(String cid) {
        return "https://".concat(website).concat("/").concat(cid).concat("/");
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(website, "\\w+", 0));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("div.cy_list_mh > ul")) {
            @Override
            protected Comic parse(Node node) {
                String title = node.text("li.title > a");
                String cid = node.hrefWithSplit("li > a", 1);
                String cover = node.attr("a.pic > img", "src");
                String update = node.text("li.updata > a > span");

                return new Comic(TYPE, cid, title, cover, update, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://" + website + "/manhua/" + cid + "/";
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String cover = body.src("img.pic");
        String intro = body.text("p#comic-description");
        String title = body.text("div.cy_title > h1");

        String update = body.text("div.cy_zhangjie_top > p >font");
        String author = body.text("div.cy_xinxi > span:eq(0)");

        // 连载状态
        boolean status = isFinish(body.text("div.cy_xinxi > span:eq(1) > a"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i = 0;
        for (Node node : new Node(html).list("div.cy_plist > ul > li")) {
            String title = node.text();
            String path = node.hrefWithSplit("a", 1);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://%s/chapter/%s.html", website, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();

        String str = StringUtils.match("\"page_url\":\"(.*?)\",", html, 1);

        int i = 0;
        for (String url : Objects.requireNonNull(str).split("\\|72cms\\|")) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            list.add(new ImageUrl(id, comicChapter, ++i, url, false));
        }

        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.cy_zhangjie_top > p >font");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "https://m.tohomh456.com");
    }

}