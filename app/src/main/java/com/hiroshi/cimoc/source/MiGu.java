package com.hiroshi.cimoc.source;

import android.util.Base64;

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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
        return StringUtils.format("http://www.migudm.cn/comic/%s.html", cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.migudm.cn"));
        filter.add(new UrlFilter("m.migudm.cn"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        return new Request.Builder().url(StringUtils.format("http://www.migudm.cn/comic/%s.html", cid)).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.inner > .ctdbRight > .ctdbRightInner > .title").trim();
        String cover = body.attr("div.inner > .ctdbLeft > a > img", "src");
//        String update = body.text("span.date").trim();
//        String author = body.text("p.author").trim();
        String intro = body.text("#worksDesc").trim();
        boolean status = false;
        comic.setInfo(title, cover, "", intro, "", status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Matcher m = Pattern.compile("<a stat='.*?' href=\"(?:.*?)(\\d+)\\.html\" class=\"item ellipsis\" title=\"(.*?)\" data-opusname=\"(?:.*?)\" data-index=\"(?:.*?)\" data-url=\"(?:.*?)\" target=\"_blank\">").matcher(html);
        int i=0;
        while (m.find()) {
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, m.group(2), m.group(1)));
        }
        return Lists.reverse(list);
    }

    public String httpGet(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(10, TimeUnit.SECONDS).build();
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("user-agent", "Mozilla/5.0 (Linux; U; Android 4.0.4; en-gb; GT-I9300 Build/IMM76D) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30")
                .get()//默认就是GET请求，可以不写
                .build();
        Call call = okHttpClient.newCall(request);
        try {
            Response response = call.execute();
            return response.body().string();
        } catch (Exception ex) {
            return "";
        }
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        _cid = cid;
        _path = path;
        String url = StringUtils.format("http://www.migudm.cn/%s/chapter/%s.html", cid, path);
        String html = httpGet(url);
        Matcher m = Pattern.compile("<input type=\"hidden\" id=\"playUrl\" value=\"(.*)\">").matcher(html);
        if (m.find()) {
            url = "http://www.migudm.cn/opus/webQueryWatchOpusInfo.html?".concat(m.group(1));
            return new Request.Builder().url(url).build();
        }
        return null;
//        String url = StringUtils.format("http://m.migudm.cn/comic/readImage.html?opusType=2&hwOpusId=090000000865&hwItemId=091000017043");
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new ArrayList<>();
        try {
            JSONObject json = new JSONObject(html);
            JSONArray jpgJsonArr = json.getJSONObject("data").getJSONArray("jpgList");
            for (int i = 0; i < jpgJsonArr.length(); i++) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);
                JSONObject j = jpgJsonArr.getJSONObject(i);
                list.add(new ImageUrl(id, comicChapter, i + 1, j.getString("url"), false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

//    @Override
//    public Request getLazyRequest(String url) {
//        return new Request.Builder()
////                .addHeader("Referer", url)
//                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
//                .addHeader("Cookie", "isAdult=1")
//                .url(url).build();
//    }
//
//    @Override
//    public String parseLazy(String html, String url) {
//        Matcher m = Pattern.compile("<\\/div><img src=\"(.*?)\" alt=").matcher(html);
//        if (m.find()) {
//            return m.group(1);
//        }
//        return null;
//    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public Headers getHeader(String url) {
        return Headers.of("Referer", url);
    }

}
