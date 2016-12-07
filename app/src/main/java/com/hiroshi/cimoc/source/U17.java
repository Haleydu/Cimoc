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
                String cid = node.hrefWithSplit("div:eq(1) > h3 > strong > a", 1);
                String title = node.attr("div:eq(1) > h3 > strong > a", "title");
                String cover = node.src("div:eq(0) > a > img");
                String update = node.textWithSubstring("div:eq(1) > h3 > span.fr", 7);
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
    public String parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.comic_info > div.left > h1.fl");
        String cover = body.src("#cover > a > img");
        String author = body.text("div.comic_info > div.right > div.author_info > div.info > a.name");
        String intro = body.text("div.comic_info > div.left > div.info > #words");
        boolean status = body.text("div.comic_info > div.left > div.info > div.top > div.line1 > span:eq(2)").contains("完结");
        String update = body.textWithSubstring("div.main > div.chapterlist > div.chapterlist_box > div.bot > div.fl > span", 7);
        comic.setInfo(title, cover, update, intro, author, status);

        return null;
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        // http://m.u17.com/chapter/list?comicId=%s
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#chapter > li > a")) {
            String title = node.text();
            String path = node.hrefWithSplit(1);
            list.add(0, new Chapter(title, path));
        }
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
        //String url = StringUtils.format("http://m.u17.com/image/list?comicId=%s&chapterId=%s", cid, path);
        String url = StringUtils.format("http://www.u17.com/chapter/%s.html", path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        /*try {
            JSONArray array = new JSONObject(html).getJSONObject("data").getJSONArray("list");
            for (int i = 0; i != array.length(); ++i) {
                JSONObject object = array.getJSONObject(i);
                list.add(new ImageUrl(i + 1, object.getString("location"), false));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
        String result = StringUtils.match("image_list: .*?\\('(.*?)'\\)", html, 1);
        try {
            JSONObject object = new JSONObject(result);
            for (int i = 1; i <= object.length(); ++i) {
                String str = object.getJSONObject(String.valueOf(i)).getString("src");
                list.add(new ImageUrl(i, DecryptionUtils.base64Decrypt(str), false));
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
        return new Node(html).textWithSubstring("div.main > div.chapterlist > div.chapterlist_box > div.bot > div.fl > span", 7);
    }

}
