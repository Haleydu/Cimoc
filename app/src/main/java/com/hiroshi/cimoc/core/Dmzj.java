package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class Dmzj extends Manga {

    public Dmzj() {
        super(Kami.SOURCE_DMZJ, "http://m.dmzj.com");
    }

    @Override
    protected String parseSearchUrl(String keyword, int page) {
        if (page == 1) {
            return "http://s.acg.178.com/comicsum/search.php?s=" + keyword;
        }
        return null;
    }

    @Override
    protected List<Comic> parseSearch(String html) {
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
    protected String parseIntoUrl(String cid) {
        return host + "/info/" + cid + ".html";
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

        Node doc = MachiSoup.body(html);
        String intro = doc.text(".txtDesc", 3);
        Node detail = doc.select(".Introduct_Sub");
        String title = detail.attr("#Cover > img", "title");
        String cover = detail.attr("#Cover > img", "src");
        String author = detail.text(".sub_r > p:eq(0) > a");
        boolean status = "已完结".equals(detail.text(".sub_r > p:eq(2) > a:eq(3)"));
        String update = detail.text(".sub_r > p:eq(3) > .date", " ", 0);
        comic.setInfo(title, cover, update, intro, author, status);

        return list;
    }

    @Override
    protected String parseBrowseUrl(String cid, String path) {
        return host + "/view/" + cid + "/" + path + ".html";
    }

    @Override
    protected String[] parseBrowse(String html) {
        String jsonString = MachiSoup.match("\"page_url\":(\\[.*?\\])", html, 1);
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                String[] images = new String[array.length()];
                for (int i = 0; i != images.length; ++i) {
                    images[i] = array.getString(i);
                }
                return images;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
