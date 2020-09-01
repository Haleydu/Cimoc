package com.hiroshi.cimoc.source;

import android.util.Base64;

import com.facebook.common.util.Hex;
import com.google.common.collect.Lists;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.JsonIterator;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;
import taobe.tec.jcc.JChineseConvertor;

public class CopyMH extends MangaParser {
    public static final int TYPE = 26;
    public static final String DEFAULT_TITLE = "拷贝漫画";
    public static final String website = "https://copymanga.com/";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public CopyMH(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = "";
        if (page == 1) {
//            JChineseConvertor jChineseConvertor = JChineseConvertor.getInstance();
//            keyword = jChineseConvertor.s2t(keyword);
            url = StringUtils.format("https://copymanga.com/api/kb/web/search/count?offset=0&platform=2&limit=50&q=%s", keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public String getUrl(String cid) {
        return "https://copymanga.com/h5/details/comic/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("copymanga.com", "\\w+", 0));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        try {
            JSONObject jsonObject = new JSONObject(html);
            return new JsonIterator(jsonObject.getJSONObject("results").getJSONObject("comic").getJSONArray("list")) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        JChineseConvertor jChineseConvertor = JChineseConvertor.getInstance();
                        String cid = object.getString("path_word");
                        String title = jChineseConvertor.t2s(object.getString("name"));
                        String cover = object.getString("cover");
                        String author = object.getJSONArray("author").getJSONObject(0).getString("name").trim();
                        return new Comic(TYPE, cid, title, cover, null, author);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://copymanga.com/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String cover = body.attr("div.comicParticulars-left-img.loadingIcon > img", "data-src");
        String intro = body.text("p.intro");
        String title = body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(1) > h6");
        String update = body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(5) > span.comicParticulars-right-txt");
        String author = body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(3) > span.comicParticulars-right-txt > a");

        // 连载状态
        String status = body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(6) > span.comicParticulars-right-txt");
        boolean finish = isFinish(status);

        comic.setInfo(title, cover, update, intro, author, finish);

        return comic;
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        String url = String.format("https://api.copymanga.com/api/v3/comic/%s/group/default/chapters?limit=500&offset=0", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) throws JSONException {
        List<Chapter> list = new LinkedList<>();
        JSONObject jsonObject = new JSONObject(html);
        JSONArray array = jsonObject.getJSONObject("results").getJSONArray("list");
        for (int i = 0; i < array.length(); ++i) {
            String title = array.getJSONObject(i).getString("name");
            String path = array.getJSONObject(i).getString("uuid");
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i), sourceComic, title, path));
        }
        return Lists.reverse(list);
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://copymanga.com/comic/%s/chapter/%s", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        Node body = new Node(html);
        String data = body.attr("div.disposableData", "disposable");
        String key = body.attr("div.disposablePass", "disposable").trim();
        String iv = data.substring(0, 0x10).trim();
        String result = data.substring(0x10).trim();
        byte[] hexCode = Hex.decodeHex(result);
        String encode = Base64.encodeToString(hexCode, 0, hexCode.length, Base64.NO_WRAP);

        try {
            String jsonString = DecryptionUtils.aesDecrypt(encode, key, iv);
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); ++i) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);
                String url = array.getJSONObject(i).getString("url");
                list.add(new ImageUrl(id, comicChapter,i + 1, url, false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.col-9.comicParticulars-title-right > ul > li:nth-child(5) > span.comicParticulars-right-txt");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", website);
    }
}
