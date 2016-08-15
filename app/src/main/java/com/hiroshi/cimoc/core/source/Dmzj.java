package com.hiroshi.cimoc.core.source;

import com.hiroshi.cimoc.core.source.base.Manga;
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class Dmzj extends Manga {

    public Dmzj() {
        super(SourceManager.SOURCE_DMZJ, "http://m.dmzj.com");
    }

    @Override
    protected Request buildSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = "http://s.acg.178.com/comicsum/search.php?s=" + keyword;
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    protected List<Comic> parseSearch(String html, int page) {
        String jsonString = MachiSoup.match("g_search_data = (.*);", html, 1);
        List<Comic> list = new LinkedList<>();
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i != array.length(); ++i) {
                    JSONObject object = array.getJSONObject(i);
                    if (object.getInt("hidden") == 1) {
                        continue;
                    }
                    String cid = object.getString("id");
                    String title = object.getString("name");
                    String cover = object.getString("cover");
                    long time = object.getLong("last_updatetime") * 1000;
                    String update = new SimpleDateFormat("yyyy-MM-dd").format(new Date(time));
                    String author = object.getString("authors");
                    boolean status = object.getInt("status_tag_id") == 2310;
                    list.add(new Comic(source, cid, title, cover, update, author, status));
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected Request buildIntoRequest(String cid) {
        String url = host + "/info/" + cid + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
        String jsonString = MachiSoup.match("\"data\":(\\[.*?\\])", html, 1);
        List<Chapter> list = new LinkedList<>();
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i != array.length(); ++i) {
                    JSONObject object = array.getJSONObject(i);
                    String c_title = object.getString("chapter_name");
                    String c_path = object.getString("id");
                    list.add(new Chapter(c_title, c_path));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Node body = MachiSoup.body(html);
        String intro = body.text(".txtDesc", 3);
        Node detail = body.select(".Introduct_Sub");
        String title = detail.attr("#Cover > img", "title");
        String cover = detail.attr("#Cover > img", "src");
        String author = detail.text(".sub_r > p:eq(0) > a");
        boolean status = "已完结".equals(detail.text(".sub_r > p:eq(2) > a:eq(3)"));
        String update = detail.text(".sub_r > p:eq(3) > .date", " ", 0);
        comic.setInfo(title, cover, update, intro, author, status);

        return list;
    }

    @Override
    protected Request buildBrowseRequest(String cid, String path) {
        String url = host + "/view/" + cid + "/" + path + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<String> parseBrowse(String html) {
        String jsonString = MachiSoup.match("\"page_url\":(\\[.*?\\]),", html, 1);
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                List<String> list = new ArrayList<>(array.length());
                for (int i = 0; i != array.length(); ++i) {
                    list.add(array.getString(i));
                }
                return list;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected Request buildCheckRequest(String cid) {
        String url = host + "/info/" + cid + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected String parseCheck(String html) {
        Node doc = MachiSoup.body(html);
        return doc.text(".Introduct_Sub > .sub_r > p:eq(3) > .date", " ", 0);
    }

}
