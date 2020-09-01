package com.hiroshi.cimoc.source;

import com.google.common.collect.Lists;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.RegexIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by FEILONG on 2017/12/21.
 */

public class Cartoonmad extends MangaParser {

    public static final int TYPE = 54;
    public static final String DEFAULT_TITLE = "动漫狂";

    public Cartoonmad(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        if (page != 1) return null;
        String url = "https://www.cartoonmad.com/search.html";
        RequestBody body = new FormBody.Builder()
                .add("keyword", URLEncoder.encode(keyword, "BIG5"))
                .add("searchtype", "all")
                .build();
        return new Request.Builder().url(url).post(body).addHeader("Referer", "https://www.cartoonmad.com/").build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Pattern pattern = Pattern.compile("<a href=comic\\/(\\d+)\\.html title=\"(.*?)\"><span class=\"covers\"><\\/span><img src=\"(.*?)\"");
        Matcher matcher = pattern.matcher(html);
        return new RegexIterator(matcher) {
            @Override
            protected Comic parse(Matcher match) {
                String cid = match.group(1);
                String title = match.group(2);
                String cover = "https://www.cartoonmad.com" + match.group(3);
//                String update = node.text("dl:eq(5) > dd");
//                String author = node.text("dl:eq(2) > dd");
                return new Comic(TYPE, cid, title, cover, "", "");
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "https://www.cartoonmad.com/comic/".concat(cid).concat(".html");
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.cartoonmad.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://www.cartoonmad.com/comic/".concat(cid).concat(".html");
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        Matcher mTitle = Pattern.compile("<meta name=\"Keywords\" content=\"(.*?),").matcher(html);
        String title = mTitle.find() ? mTitle.group(1) : "";
        Matcher mCover = Pattern.compile("<div class=\"cover\"><\\/div><img src=\"(.*?)\"").matcher(html);
        String cover = mCover.find() ? "https://www.cartoonmad.com" + mCover.group(1) : "";
        String update = "";
        String author = "";
        Matcher mInro = Pattern.compile("<META name=\"description\" content=\"(.*?)\">").matcher(html);
        String intro = mInro.find() ? mInro.group(1) : "";
        boolean status = false;
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Matcher mChapter = Pattern.compile("<a href=(.*?) target=_blank>(.*?)<\\/a>&nbsp;").matcher(html);
        int i=0;
        while (mChapter.find()) {
            String title = mChapter.group(2);
            String path = mChapter.group(1);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return Lists.reverse(list);
    }

    private String _cid, _path;

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://www.cartoonmad.com%s", path);
        _cid = cid;
        _path = path;
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html,Chapter chapter) {
        List<ImageUrl> list = new ArrayList<>();
        Matcher pageMatcher = Pattern.compile("<a class=onpage>.*<a class=pages href=(.*)\\d{3}\\.html>(.*?)<\\/a>").matcher(html);
        if (!pageMatcher.find()) return null;
        int page = Integer.parseInt(pageMatcher.group(2));
        for (int i = 1; i <= page; ++i) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            list.add(new ImageUrl(id, comicChapter, i, StringUtils.format("https://www.cartoonmad.com/comic/%s%03d.html", pageMatcher.group(1), i), true));
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder()
                .addHeader("Referer", url)
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .url(url).build();
    }

    @Override
    public String parseLazy(String html, String url) {
        Matcher m = Pattern.compile("<img src=\"(.*?)\" border=\"0\" oncontextmenu").matcher(html);
        if (m.find()) {
            return "https://www.cartoonmad.com/comic/" + m.group(1);
        }
        return null;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://www.cartoonmad.com");
    }

}
