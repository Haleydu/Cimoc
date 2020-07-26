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

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by ZhiWen on 2019/02/25.
 */

public class TuHao extends MangaParser {

    public static final int TYPE = 24;
    public static final String DEFAULT_TITLE = "土豪漫画";
    public static final String website = "tuhao456.com";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public TuHao(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
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
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String cover = body.src("img.pic");
        String intro = body.text("p#comic-description");
        String title = body.text("div.cy_title > h1");

        String update = "";
        String author = body.text("div.cy_xinxi > span:eq(0)");

        // 连载状态
        boolean status = isFinish(body.text("div.cy_xinxi > span:eq(1) > a"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("div.cy_plist > ul > li")) {
            String title = node.text();
            String path = node.hrefWithSplit("a", 1);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://%s/chapter/%s.html", website, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();

        String str = StringUtils.match("\"page_url\":\"(.*?)\",", html, 1);

        int i = 0;
        for(String url : str.split("\\|72cms\\|")) {
            list.add(new ImageUrl(i + 1, url, false));
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
    public Headers getHeader() {
        return Headers.of("Referer", "https://m.tohomh456.com");
    }

}
