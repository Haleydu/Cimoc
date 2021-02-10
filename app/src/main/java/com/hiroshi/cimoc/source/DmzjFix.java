package com.hiroshi.cimoc.source;

import android.annotation.SuppressLint;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.JsonIterator;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;
import com.hiroshi.cimoc.utils.UicodeBackslashU;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;
import okhttp3.Request;

public class DmzjFix extends MangaParser {
    public static final int TYPE = 100;
    public static final String DEFAULT_TITLE = "动漫之家v2Fix";

    public DmzjFix(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("manhua.dmzj.com", "/(\\w+)"));
        filter.add(new UrlFilter("m.dmzj.com", "/info/(\\w+).html"));
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException, Exception {
        if (page == 1) {
            String url = StringUtils.format("https://m.dmzj.com/search/%s.html", keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public String getUrl(String cid) {
        return StringUtils.format("http://m.dmzj.com/info/%s.html", cid);
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        try {
            String JsonString = StringUtils.match("var serchArry=(\\[\\{.*?\\}\\])", html, 1);
            String decodeJsonString = UicodeBackslashU.unicodeToCn(JsonString).replace("\\/", "/");
            return new JsonIterator(new JSONArray(decodeJsonString)) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        String cid = object.getString("id");
                        String title = object.getString("name");
                        String cover = object.getString("cover");
                        cover = "https://images.dmzj.com/" + cover;
                        String author = object.optString("authors");
                        long time = Long.parseLong(object.getString("last_updatetime")) * 1000;
                        String update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(time));
                        return new Comic(TYPE, cid, title, cover, update, author);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://api.dmzj.com/dynamic/comicinfo/%s.json", cid);
        return new Request.Builder().url(url).build();
    }

    public Headers getHeader() {
        return Headers.of("Referer", "http://images.dmzj.com/");
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        try {
            JSONObject root = new JSONObject(html).getJSONObject("data");
            JSONObject info = root.getJSONObject("info");
            String title = info.getString("title");
            boolean status = !info.getString("status").contains("连载");
            String cover = info.getString("cover");
            String author = info.getString("authors");
            String update = info.getString("last_updatetime");
            update = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Integer.parseInt(update) * 1000));
            String intro = info.getString("description");
            comic.setInfo(title, cover, update, intro, author, status);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        List<Chapter> list1 = new LinkedList<>();

        try {
            JSONArray root = new JSONObject(html).getJSONObject("data").getJSONArray("list");
            for (int i = 0; i < root.length(); i++) {
                JSONObject chapter = root.getJSONObject(i);
                String title = chapter.getString("chapter_name");
                String comic_id = chapter.getString("comic_id");
                String chapter_id = chapter.getString("id");
                String path = comic_id + "/" + chapter_id;
                list.add(new Chapter(Long.parseLong(sourceComic + "000" + i + 1), sourceComic, title, path, "默认线路"));
                list1.add(new Chapter(Long.parseLong(sourceComic + "001" + i + 1), sourceComic, title.concat(" (备用)"), path + "x", "备用线路"));


            }
            list.addAll(list1);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;

        }
        return list;


    }

    @Override
    public Request getImagesRequest(String cid, String path) {

        String url = StringUtils.format("https://m.dmzj.com/chapinfo/%s.html", path.replace("x", ""));
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) throws Manga.NetworkErrorException, JSONException {
        List<ImageUrl> list = new LinkedList<>();
        JSONArray root = new JSONObject(html).getJSONArray("page_url");
        String flag = chapter.getId().toString().replace(chapter.getSourceComic().toString(), "");
        for (int i = 0; i < root.length(); i++) {
            Long comicChapter = chapter.getId();
            String url = root.getString(i);
            Long id = Long.parseLong(comicChapter + "000" + i);

            if (flag.startsWith("001")) {
                url = url.replace("dmzj", "dmzj1");

            }
            list.add(new ImageUrl(id, comicChapter, i + 1, url, false));


        }


        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public String parseCheck(String html) {
        try {
            String update = new JSONObject(html).getJSONObject("data").getString("last_updatetime");
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(Integer.parseInt(update) * 1000));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

}
