package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.JsonIterator;
import com.hiroshi.cimoc.core.parser.MangaParser;
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

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Hiroshi on 2016/8/25.
 */
public class DM5 extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = "http://m.dm5.com/pagerdata.ashx";
        RequestBody body = new FormBody.Builder()
                .add("t", "7")
                .add("pageindex", String.valueOf(page))
                .add("title", keyword)
                .build();
        return new Request.Builder().url(url).post(body).addHeader("Referer", "http://m.dm5.com").build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        try {
            return new JsonIterator(new JSONArray(html)) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        String cid = object.getString("Url").split("/")[1];
                        String title = object.getString("Title");
                        String cover = object.getString("Pic");
                        String update = object.getString("LastPartTime");
                        JSONArray array = object.optJSONArray("Author");
                        String author = "";
                        for (int i = 0; array != null && i != array.length(); ++i) {
                            author = author.concat(array.optString(i));
                        }
                        return new Comic(SourceManager.SOURCE_DM5, cid, title, cover, update, author);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://www.dm5.com/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public String parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("#mhinfo > div.inbt > h1.new_h2");
        String cover = body.attr("#mhinfo > div.innr9 > div.innr90 > div.innr91 > img", "src");
        String update = body.text("#mhinfo > div.innr9 > div.innr90 > div.innr92 > span:eq(9)", 5, -10);
        String author = body.text("#mhinfo > div.innr9 > div.innr90 > div.innr92 > span:eq(2) > a");
        String intro = body.text("#mhinfo > div.innr9 > div.mhjj > p").replace("[+展开]", "").replace("[-折叠]", "");
        boolean status = "已完结".equals(body.text("#mhinfo > div.innr9 > div.innr90 > div.innr92 > span:eq(6)", 5));
        comic.setInfo(title, cover, update, intro, author, status);

        return StringUtils.match("var DM5_COMIC_MID=(\\d+?);", html, 1);
    }

    @Override
    public Request getChapterRequest(String mid) {
        String url = StringUtils.format("http://www.dm5.com/template-%s-t2-s2", mid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        Set<Chapter> set = new LinkedHashSet<>();
        Node body = new Node(html);
        for (Node node : body.list("ul.nr6 > li > a[title]")) {
            String title = node.text();
            String path = node.attr("href", "/", 1);
            set.add(new Chapter(title, path));
        }
        return new LinkedList<>(set);
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = "http://m.dm5.com/manhua-new/pagerdata.ashx";
        RequestBody body = new FormBody.Builder()
                .add("t", "2")
                .add("pageindex", String.valueOf(page))
                .build();
        return new Request.Builder().url(url).post(body).addHeader("Referer", "http://m.dm5.com").build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        try {
            JSONArray array = new JSONArray(html);
            for (int i = 0; i != array.length(); ++i) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    String cid = object.getString("Url").split("/")[1];
                    String title = object.getString("Title");
                    String cover = object.getString("Pic");
                    String update = object.getString("LastPartTime");
                    JSONArray temp = object.optJSONArray("Author");
                    String author = "";
                    for (int j = 0; temp != null && j != temp.length(); ++j) {
                        author = author.concat(temp.optString(j));
                    }
                    list.add(new Comic(SourceManager.SOURCE_DM5, cid, title, cover, update, author));
                } catch (JSONException e) {
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
        String url = "http://www.dm5.com/".concat(path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String[] rs = StringUtils.match("var DM5_CID=(.*?);\\s*var DM5_IMAGE_COUNT=(\\d+);", html, 1, 2);
        if (rs != null) {
            String format = "http://www.dm5.com/m%s/chapterfun.ashx?cid=%s&page=%d";
            String packed = StringUtils.match("eval(.*?)\\s*</script>", html, 1);
            if (packed != null) {
                String key = StringUtils.match("comic=(.*?);", DecryptionUtils.evalDecrypt(packed), 1);
                if (key != null) {
                    key = key.replaceAll("'|\\+", "");
                    format = format.concat("&key=").concat(key);
                }
            }
            int page = Integer.parseInt(rs[1]);
            for (int i = 0; i != page; ++i) {
                list.add(new ImageUrl(i + 1, StringUtils.format(format, rs[0], rs[0], i + 1), true));
            }
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder().url(url)
                .addHeader("Referer", "http://www.dm5.com")
                .addHeader("Accept-Language", "en-us,en")
                .addHeader("X-Forwarded-For", "003.000.000.000")
                .build();
    }

    @Override
    public String parseLazy(String html, String url) {
        if (html == null) {
            return null;
        }
        String result = DecryptionUtils.evalDecrypt(html);
        if (result != null) {
            return result.split(",")[0];
        }
        return null;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("#mhinfo > div.innr9 > div.innr90 > div.innr92 > span:eq(9)", 5, -10);
    }

}
