package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.JsonIterator;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class Dmzj extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = "http://s.acg.178.com/comicsum/search.php?s=".concat(keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        String jsonString = StringUtils.match("g_search_data = (.*);", html, 1);
        try {
            return new JsonIterator(new JSONArray(jsonString)) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        if (object.optInt("hidden", 1) != 1) {
                            String cid = object.getString("id");
                            String title = object.getString("name");
                            String cover = object.getString("cover");
                            long time = object.getLong("last_updatetime") * 1000;
                            String update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(time));
                            String author = object.optString("authors");
                            // boolean status = object.getInt("status_tag_id") == 2310;
                            return new Comic(SourceManager.SOURCE_DMZJ, cid, title, cover, update, author);
                        }
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
        String url = StringUtils.format("http://m.dmzj.com/info/%s.html", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public String parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String intro = body.textWithSubstring("p.txtDesc", 3);
        String title = body.attr("#Cover > img", "title");
        String cover = body.src("#Cover > img");
        String author = body.text("div.Introduct_Sub > div.sub_r > p:eq(0) > a");
        String update = body.textWithSubstring("div.Introduct_Sub > div.sub_r > p:eq(3) > span.date", 0, 10);
        boolean status = body.text("div.Introduct_Sub > div.sub_r > p:eq(2) > a:eq(3)").contains("完结");
        comic.setInfo(title, cover, update, intro, author, status);

        return null;
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        String jsonString = StringUtils.match("initIntroData\\((.*?)\\);", html, 1);
        List<Chapter> list = new LinkedList<>();
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i != array.length(); ++i) {
                    JSONArray data = array.getJSONObject(i).getJSONArray("data");
                    for (int j = 0; j != data.length(); ++j) {
                        JSONObject object = data.getJSONObject(j);
                        String title = object.getString("chapter_name");
                        String path = object.getString("id");
                        list.add(new Chapter(title, path));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.dmzj.com/view/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String jsonString = StringUtils.match("\"page_url\":(\\[.*?\\]),", html, 1);
        if (jsonString != null) {
            try {
                JSONArray array = new JSONArray(jsonString);
                for (int i = 0; i != array.length(); ++i) {
                    list.add(new ImageUrl(i + 1, array.getString(i), false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = StringUtils.format("http://m.dmzj.com/latest/%d.json", (page - 1));
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        try {
            JSONArray array = new JSONArray(html);
            for (int i = 0; i != array.length(); ++i) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    if (object.optInt("hidden", 1) != 1) {
                        String cid = object.getString("id");
                        String title = object.getString("name");
                        String cover = "http://images.dmzj.com/".concat(object.getString("cover"));
                        long time = object.getLong("last_updatetime") * 1000;
                        String update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(time));
                        String author = object.optString("authors");
                        // boolean status = object.getInt("status_tag_id") == 2310;
                        list.add(new Comic(SourceManager.SOURCE_DMZJ, cid, title, cover, update, author));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
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
        return new Node(html).textWithSubstring("div.Introduct_Sub > div.sub_r > p:eq(3) > span.date", 0, 10);
    }

}
