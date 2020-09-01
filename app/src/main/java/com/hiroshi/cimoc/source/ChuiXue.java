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
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by ZhiWen on 2019/02/25.
 */

public class ChuiXue extends MangaParser {

    public static final int TYPE = 69;
    public static final String DEFAULT_TITLE = "吹雪漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public ChuiXue(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("http://www.chuixue.net/e/search/?searchget=1&show=title,player,playadmin,pinyin&keyboard=%s",
                    URLEncoder.encode(keyword, "GB2312"));
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public String getUrl(String cid) {
        return "http://www.chuixue.net/manhua/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.chuixue.net"));
    }


    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#dmList> ul > li")) {
            @Override
            protected Comic parse(Node node) {
                Node node_cover = node.list("p > a").get(0);
                String cid = node_cover.hrefWithSplit(1);
                String title = node_cover.attr("img", "alt");
                String cover = node_cover.attr("img", "_src");
                String update = node.text("dl > dd > p:eq(0) > span");
                return new Comic(TYPE, cid, title, cover, update, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://www.chuixue.net/manhua/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.intro_l > div.title > h1");
        String cover = body.src("div.intro_l > div.info_cover > p.cover > img");
        String update = body.text("div.intro_l > div.info > p:eq(0) > span").substring(0, 10);
        String author = body.text("div.intro_l > div.info > p:eq(1)").substring(5).trim();
        String intro = body.text("#intro");
        boolean status = isFinish(body.text("div.intro_l > div.info > p:eq(2)"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("#play_0 > ul > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    private String _cid = "";
    private String _path = "";

    @Override
    public Request getImagesRequest(String cid, String path) {
        _cid = cid;
        _path = path;

        String url = StringUtils.format("http://www.chuixue.net/manhua/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("photosr\\[1\\](.*?)(\n|var)", html, 1);
        if (str != null) {
            try {
                String[] array = str.split(";");
                for (int i = 0; i != array.length; ++i) {
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    String s_full = array[i].trim();
                    int index = s_full.indexOf("=");
                    String s = s_full.substring(index + 2, s_full.length() - 1);
                    list.add(new ImageUrl(id, comicChapter,i + 1, "http://chuixue1.tianshigege.com/" + s, false));
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
        // 这里表示的是更新时间
        return new Node(html).text("div.intro_l > div.info > p:eq(0) > span").substring(0, 10);
    }

    @Override
    public Headers getHeader() {
        String referer = StringUtils.format("http://www.chuixue.net/manhua/%s/%s.html", _cid, _path);

        return Headers.of("Referer", referer);
    }

}
