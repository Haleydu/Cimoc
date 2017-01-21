package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.parser.JsonIterator;
import com.hiroshi.cimoc.parser.MangaCategory;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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

    public DM5() {
        category = new Category();
    }

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
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("#mhinfo > div.inbt > h1.new_h2");
        String cover = body.src("#mhinfo > div.innr9 > div.innr90 > div.innr91 > img");
        String str = body.text("#mhinfo > div.innr9 > div.innr90 > div.innr92");
        String[] args = StringUtils.match("漫画作者：(.*?) .* 漫画状态：(.*?) .* 更新时间：(.*?) ", str, 1, 2, 3);
        String update = args == null ? null : args[2];
        String author = args == null ? null : args[0];
        String intro = body.text("#mhinfo > div.innr9 > div.mhjj > p");
        if (intro != null) {
            intro = intro.replace("[+展开]", "").replace("[-折叠]", "");
        }
        boolean status = isFinish(args == null ? null : args[1]);
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        String id = StringUtils.match("var DM5_COMIC_MID=(\\d+?);", html, 1);
        String url = StringUtils.format("http://www.dm5.com/template-%s-t2-s2", id);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        Set<Chapter> set = new LinkedHashSet<>();
        Node body = new Node(html);
        for (Node node : body.list("ul.nr6 > li > a[title]")) {
            String title = node.text();
            String path = node.hrefWithSplit(0);
            set.add(new Chapter(title, path));
        }
        return new LinkedList<>(set);
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
        return new Node(html).textWithSubstring("#mhinfo > div.innr9 > div.innr90 > div.innr92 > span:eq(9)", 5, -10);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new ArrayList<>();
        Node body = new Node(html);
        for (Node node : body.list("#index_left > div.inkk > div.innr3 > li")) {
            String cid = node.hrefWithSplit("a", 0);
            String title = node.attr("a", "title");
            String cover = node.src("a > img");
            String[] args = StringUtils.match("漫画家：(.*?)\\[(.*?)：", node.text(), 1, 2);
            String update = args == null ? null : args[1];
            if (update != null) {
                update = update.replaceAll("[年月日]", " ").trim().replaceAll(" ", "-");
            }
            String author = args == null ? null : args[0];
            list.add(new Comic(SourceManager.SOURCE_DM5, cid, title, cover, update, author));
        }
        return list;
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            String path = args[0].concat(" ").concat(args[1]).concat(" ").concat(args[4])
                    .concat(" ").concat(args[5]).trim();
            path = path.replaceAll("\\s+", "-");
            return StringUtils.format("http://www.dm5.com/manhua-list-%s-size40-p%%d", path);
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("少年热血", "cg39"));
            list.add(Pair.create("少女爱情", "cg40"));
            list.add(Pair.create("武侠格斗", "cg41"));
            list.add(Pair.create("科幻魔幻", "cg42"));
            list.add(Pair.create("竞技体育", "cg43"));
            list.add(Pair.create("爆笑喜剧", "cg44"));
            list.add(Pair.create("侦探推理", "cg45"));
            list.add(Pair.create("其它漫画", "cg47"));
            list.add(Pair.create("东方同人", "cg55"));
            return list;
        }

        @Override
        protected boolean hasArea() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getArea() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("港台", "area35"));
            list.add(Pair.create("日韩", "area36"));
            list.add(Pair.create("内地", "area37"));
            list.add(Pair.create("欧美", "area38"));
            return list;
        }

        @Override
        public boolean hasProgress() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getProgress() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("连载", "st1"));
            list.add(Pair.create("完结", "st2"));
            return list;
        }

        @Override
        protected boolean hasOrder() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("更新", "s2"));
            list.add(Pair.create("人气", "s4"));
            list.add(Pair.create("评论", "s6"));
            return list;
        }

    }

}
