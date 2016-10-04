package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.core.parser.NodeIterator;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
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
 * Created by Hiroshi on 2016/8/8.
 */
public class U17 extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("http://so.u17.com/all/%s/m0_p%d.html", keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#comiclist > div.search_list > div.comiclist > ul > li > div")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr("div:eq(1) > h3 > strong > a", "href", "/|\\.", 6);
                String title = node.attr("div:eq(1) > h3 > strong > a", "title");
                String cover = node.attr("div:eq(0) > a > img", "src");
                String update = node.text("div:eq(1) > h3 > span.fr", 7);
                String author = node.text("div:eq(1) > h3 > a[title]");
                // String[] array = node.text("div:eq(1) > p.cf > i.fl").split("/");
                // boolean status = "已完结".equals(array[array.length - 1].trim());
                return new Comic(SourceManager.SOURCE_U17, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://www.u17.com/comic/%s.html", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseInfo(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#chapter > li > a")) {
            String c_title = node.text().trim();
            String c_path = node.attr("href", "/|\\.", 6);
            list.add(0, new Chapter(c_title, c_path));
        }

        String title = body.text("div.comic_info > div.left > h1.fl").trim();
        String cover = body.attr("#cover > a > img", "src");
        String author = body.text("div.comic_info > div.right > div.author_info > div.info > a.name");
        String intro = body.text("div.comic_info > div.left > div.info > #words").trim();
        boolean status = "已完结".equals(body.text("div.main > div.info > div.fl > span.eq(2)"));
        String update = body.text("div.main > div.chapterlist > div.chapterlist_box > div.bot > div.fl > span", 7);
        comic.setInfo(title, cover, update, intro, author, status);
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = StringUtils.format("http://m.u17.com/update/list/%d?page=0&pageSize=1000", (page - 1));
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        try {
            JSONArray array = new JSONArray(html);
            int size = array.length();
            for (int i = 0; i != size; ++i) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    String cid = object.getString("comicId");
                    String title = object.getString("name");
                    String cover = object.getString("cover");
                    long time = object.getLong("lastUpdateTime") * 1000;
                    String update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(time));
                    String author = object.optString("authorName");
                    list.add(new Comic(SourceManager.SOURCE_U17, cid, title, cover, update, author));
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
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://www.u17.com/chapter/%s.html", path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        List<String> result = StringUtils.matchAll("\"src\":\"(.*?)\"", html, 1);
        if (!result.isEmpty()) {
            try {
                int count = 0;
                for (String str : result) {
                    list.add(new ImageUrl(++count, DecryptionUtils.base64Decrypt(str), false));
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
        return new Node(html).text("div.main > div.chapterlist > div.chapterlist_box > div.bot > div.fl > span", 7);
    }

}
