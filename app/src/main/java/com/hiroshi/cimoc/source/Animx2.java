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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by FEILONG on 2017/12/21.
 */

public class Animx2 extends MangaParser {

    public static final int TYPE = 55;
    public static final String DEFAULT_TITLE = "2animx";

    public Animx2(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        if (page != 1) return null;
        String url = StringUtils.format("http://www.2animx.com/search-index?searchType=1&q=%s&page=%d", keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("ul.liemh > li")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit("a", 0);
                String title = node.text("a > div.tit");
                String cover = "http://www.2animx.com" + node.attr("a > img", "src");
                String update = node.text("a > font");
                return new Comic(TYPE, cid, title, cover, update, "");
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.bnmanhua.com", ".*", 0));
    }

    @Override
    public Request getInfoRequest(String cid) {
        if (cid.indexOf("http://www.2animx.com") == -1) {
            cid = "http://www.2animx.com/".concat(cid);
        }
        return new Request.Builder().url(cid).addHeader("Cookie", "isAdult=1").build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.position > strong");
        String cover = "http://www.2animx.com/" + body.src("dl.mh-detail > dt > a > img");
        String update = "";
        String author = "";
        String intro = body.text(".mh-introduce");
        boolean status = false;
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("div#oneCon2 > ul > li")) {
            String title = node.attr("a", "title");
            Matcher mTitle = Pattern.compile("\\d+").matcher(title);
            title = mTitle.find() ? mTitle.group() : title;
            String path = node.hrefWithSplit("a", 0);

            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    private String _cid, _path;

    @Override
    public Request getImagesRequest(String cid, String path) {
        if (path.indexOf("http://www.2animx.com") == -1) {
            path = "http://www.2animx.com/".concat(path);
        }
        _cid = cid;
        _path = path;
        return new Request.Builder().url(path).addHeader("Cookie", "isAdult=1").build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new ArrayList<>();
        Matcher pageMatcher = Pattern.compile("id=\"total\" value=\"(.*?)\"").matcher(html);
        if (!pageMatcher.find()) return null;
        int page = Integer.parseInt(pageMatcher.group(1));
        for (int i = 1; i <= page; ++i) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            list.add(new ImageUrl(id, comicChapter, i, StringUtils.format("%s-p-%d", _path, i), true));
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder()
//                .addHeader("Referer", url)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .addHeader("Cookie", "isAdult=1")
                .url(url).build();
    }

    @Override
    public String parseLazy(String html, String url) {
        Matcher m = Pattern.compile("<\\/div><img src=\"(.*?)\" alt=").matcher(html);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public Headers getHeader(String url) {
        return Headers.of("Referer", url);
    }

}
