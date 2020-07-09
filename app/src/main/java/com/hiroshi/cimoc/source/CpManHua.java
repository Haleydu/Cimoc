package com.hiroshi.cimoc.source;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
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
import com.hiroshi.cimoc.utils.HttpUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Request;

public class CpManHua extends MangaParser {

    public static final int TYPE = 72;
    public static final String DEFAULT_TITLE = "拷贝漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public CpManHua(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        if (page != 1) return null;
        String url = StringUtils.format("https://www.copymanga.com/search?q=%s", keyword);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        Node body = new Node(html);
        return new NodeIterator(body.list("ul#listbody > li")) {
            @Override
            protected Comic parse(Node node) {
                final String cid = node.href("a.ImgA");
                final String title = node.text("a.txtA");
                final String cover = node.attr("a.ImgA > img", "src");
                return new Comic(TYPE, cid, title, cover, "", "");
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "https://www.copymanga.com" + cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.copymanga.com", ".*", 0));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("https://www.copymanga.com/%s/", cid);
        return HttpUtils.getSimpleMobileRequest(url);
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String intro = body.text("#full-des");
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
        path = StringUtils.format("https://www.copymanga.com%s/%s.html", cid, path);
        return new Request.Builder().url(path).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new ArrayList<>();
        Matcher pageMatcher = Pattern.compile("qTcms_S_m_murl_e=\"(.*?)\"").matcher(html);
        if (!pageMatcher.find()) return null;
        try {
            final String imgArrStr = DecryptionUtils.base64Decrypt(pageMatcher.group(1));
            int i = 0;
            for (String item : imgArrStr.split("\\$.*?\\$")) {
                list.add(new ImageUrl(i++, item, false));
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
        return Headers.of("Referer", "https://www.copymanga.com/");
    }
}
