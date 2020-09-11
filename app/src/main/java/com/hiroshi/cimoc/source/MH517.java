package com.hiroshi.cimoc.source;

import android.util.Log;

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
import com.hiroshi.cimoc.utils.LogUtil;
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

public class MH517 extends MangaParser {

    public static final int TYPE = 70;
    public static final String DEFAULT_TITLE = "我要去漫画";

    public MH517(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        if (page != 1) return null;
        String url = StringUtils.format("http://m.517manhua.com/statics/search.aspx?key=%s", keyword);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("ul#listbody > li")) {
            @Override
            protected Comic parse(Node node) {
                final String cid = node.href("a.ImgA");
                final String title = node.text("a.txtA");
                final String cover = node.attr("a.ImgA > img", "src");
                final String update = node.text("");
                final String author = node.text("");
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "http://m.517manhua.com" + cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.517manhua.com", ".*", 0));
    }

    @Override
    public Request getInfoRequest(String cid) {
        if (cid.indexOf("http://m.517manhua.com") == -1) {
            cid = "http://m.517manhua.com".concat(cid);
        }
        return new Request.Builder().url(cid).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.attr("div#Cover > img", "title");
        String cover = body.src("div#Cover > img");
        String update = body.text("span.date" );
        String author = body.text("");
        String intro = body.text("p.txtDesc");
        boolean status = false;
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("#mh-chapter-list-ol-0 > li")) {
            String title = node.text("a > span");
            String path = node.hrefWithSplit("a", 2);

            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }


    @Override
    public Request getImagesRequest(String cid, String path) {
        path = StringUtils.format("http://m.517manhua.com%s/%s.html", cid, path);
        return new Request.Builder().url(path).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new ArrayList<>();
        Matcher pageMatcher = Pattern.compile("qTcms_S_m_murl_e=\"(.*?)\"").matcher(html);
        final String mangaid = StringUtils.match("var qTcms_S_m_id=\"(\\w+?)\";", html, 1);
        if (!pageMatcher.find()) return null;
        try {
            final String imgArrStr = DecryptionUtils.base64Decrypt(pageMatcher.group(1));
            int i = 0;
            for (String item : imgArrStr.split("\\$.*?\\$")) {
                final String url = "http://m.517manhua.com/statics/pic/?p=" + item + "&wapif=1&picid=" + mangaid + "&m_httpurl=";

                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);
                list.add(new ImageUrl(id, comicChapter, ++i, url, false));
            }
        } finally {
            return list;
        }
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.517manhua.com/");
    }

}
