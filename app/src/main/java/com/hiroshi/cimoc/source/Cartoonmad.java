package com.hiroshi.cimoc.source;

import android.util.Pair;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaCategory;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.RegexIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
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
        String url = "https://www.cartoonmad.com/search.html";
        RequestBody body = new FormBody.Builder()
                .add("keyword", URLEncoder.encode(keyword, "BIG5"))
                .add("searchtype", "all")
                .build();
        return new Request.Builder().url(url).post(body).addHeader("Referer", "https://www.cartoonmad.com/").build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        try {
            html = new String(html.getBytes(), "BIG5");
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
        } catch (UnsupportedEncodingException ex){
            return null;
        }
    }

    @Override
    public String getUrl(String cid) {
        return "http://m.pufei.net/manhua/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.pufei.net"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.pufei.net/manhua/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.main-bar > h1");
        String cover = body.src("div.book-detail > div.cont-list > div.thumb > img");
        String update = body.text("div.book-detail > div.cont-list > dl:eq(2) > dd");
        String author = body.text("div.book-detail > div.cont-list > dl:eq(3) > dd");
        String intro = body.text("#bookIntro");
        boolean status = isFinish(body.text("div.book-detail > div.cont-list > div.thumb > i"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#chapterList2 > ul > li > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.pufei.net/manhua/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("cp=\"(.*?)\"", html, 1);
        if (str != null) {
            try {
                str = DecryptionUtils.evalDecrypt(DecryptionUtils.base64Decrypt(str));
                String[] array = str.split(",");
                for (int i = 0; i != array.length; ++i) {
                    list.add(new ImageUrl(i + 1, "http://f.pufei.net/" + array[i], false));
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
        for (Node node : body.list("li > a")) {
            String cid = node.hrefWithSplit(1);
            String title = node.text("h3");
            String cover = node.attr("div > img", "data-src");
            String update = node.text("dl:eq(5) > dd");
            String author = node.text("dl:eq(2) > dd");
            list.add(new Comic(TYPE, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.pufei.net");
    }

}
