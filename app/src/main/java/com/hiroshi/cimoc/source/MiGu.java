package com.hiroshi.cimoc.source;

import android.util.Base64;

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
import java.nio.charset.Charset;
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

public class MiGu extends MangaParser {

    public static final int TYPE = 58;
    public static final String DEFAULT_TITLE = "咪咕漫画";
    private String _cid, _path;

    public MiGu(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        //这编码牛逼,不认识...
        //天 => JUU1JUE0JUE5
        //好吧,搞出来了
        //天 ===utf-8===> \xE5\xA4\xA9 ===\x->%=====> %E5%A1%A9 ===base64=====> JUU1JUE0JUE5
        byte[] keywordByte = keyword.getBytes(Charset.forName("UTF-8"));
        String keyPre = "";
        for (byte k : keywordByte) {
            keyPre += "%" + String.format("%02x", k);
        }
        String keywordconv = Base64.encodeToString(keyPre.toUpperCase().getBytes(), keyPre.length()).trim();
        String url = StringUtils.format("http://www.migudm.cn/search/result/list.html?hintKey=%s&hintType=2&pageSize=30&pageNo=%d",
                keywordconv,
                page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Matcher m = Pattern.compile("href=\"\\/comic\\/(.*?).html\"  title=\"(.*?)\">\\s+<img src=\"(.*?)\".*?>").matcher(html);
        return new RegexIterator(m) {
            @Override
            protected Comic parse(Matcher match) {
                return new Comic(TYPE, match.group(1), match.group(2), match.group(3), "", "");
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.2animx.com", ".*", 0));
    }

    @Override
    public Request getInfoRequest(String cid) {
        if (cid.indexOf("http://www.2animx.com") == -1) {
            cid = "http://www.2animx.com/".concat(cid);
        }
        return new Request.Builder().url(cid).addHeader("Cookie", "isAdult=1").build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.position > strong");
        String cover = "http://www.2animx.com/" + body.src("dl.mh-detail > dt > a > img");
        String update = "";
        String author = "";
        String intro = body.text(".mh-introduce");
        boolean status = false;
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("div#oneCon2 > ul > li")) {
            String title = node.attr("a", "title");
            Matcher mTitle = Pattern.compile("\\d+").matcher(title);
            title = mTitle.find() ? mTitle.group() : title;
            String path2 = node.href("a");
            String path = node.hrefWithSplit("a", 0);
            list.add(new Chapter(title, path));
        }
        return list;
    }

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
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new ArrayList<>();
        Matcher pageMatcher = Pattern.compile("id=\"total\" value=\"(.*?)\"").matcher(html);
        if (!pageMatcher.find()) return null;
        int page = Integer.parseInt(pageMatcher.group(1));
        for (int i = 1; i <= page; ++i) {
            list.add(new ImageUrl(i, StringUtils.format("%s-p-%d", _path, i), true));
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
