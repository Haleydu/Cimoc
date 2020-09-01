package com.hiroshi.cimoc.source;

import android.util.Log;
import android.util.Pair;

import com.google.common.collect.Lists;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.JsonIterator;
import com.hiroshi.cimoc.parser.MangaCategory;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Hiroshi on 2016/8/25.
 */
public class DM5 extends MangaParser {

    public static final int TYPE = 5;
    public static final String DEFAULT_TITLE = "动漫屋";

    public DM5(Source source) {
        init(source, new Category());
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
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
                        return new Comic(TYPE, cid, title, cover, update, author);
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
    public String getUrl(String cid) {
        return "http://www.dm5.com/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.dm5.com", "/([\\w\\-]+)"));
        filter.add(new UrlFilter("tel.dm5.com", "/([\\w\\-]+)"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://www.dm5.com/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.textWithSplit("div.banner_detail_form > div.info > p.title", " ", 0);
        String cover = body.src("div.banner_detail_form > div.cover > img");
        String update = body.text("#tempc > div.detail-list-title > span.s > span");
        if (update != null) {
            Calendar calendar = Calendar.getInstance();
            if (update.contains("今天") || update.contains("分钟前")) {
                update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            } else if (update.contains("昨天")) {
                calendar.add(Calendar.DATE, -1);
                update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            } else if (update.contains("前天")) {
                calendar.add(Calendar.DATE, -2);
                update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            } else {
                String result = StringUtils.match("\\d+-\\d+-\\d+", update, 0);
                if (result == null) {
                    String[] rs = StringUtils.match("(\\d+)月(\\d+)号", update, 1, 2);
                    if (rs != null) {
                        result = calendar.get(Calendar.YEAR) + "-" + rs[0] + "-" + rs[1];
                    }
                }
                update = result;
            }
        }
        String author = body.text("div.banner_detail_form > div.info > p.subtitle > a");
        String intro = body.text("div.banner_detail_form > div.info > p.content");
        if (intro != null) {
            intro = intro.replace("[+展开]", "").replace("[-折叠]", "");
        }
        boolean status = isFinish(body.text("div.banner_detail_form > div.info > p.tip > span:eq(0)"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int i=0;
        for (Node node : body.list("#chapterlistload > ul  li > a")) {
            String title = StringUtils.split(node.text(), " ", 0);
            String path = node.hrefWithSplit(0);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return Lists.reverse(list);
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = "http://m.dm5.com/".concat(path);
        return new Request
                .Builder()
                .addHeader("Referer", StringUtils.format("http://m.dm5.com/%s", path))
                .url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("eval\\(.*\\)", html, 0);
        if (str != null) {
            try {
                str = DecryptionUtils.evalDecrypt(str, "newImgs");
                String[] array = str.split(",");
                for (int i = 0; i != array.length; ++i) {
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    list.add(new ImageUrl(id, comicChapter,i + 1, array[i], false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder().url(url)
                .addHeader("Referer", "http://www.dm5.com")
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
        Node body = new Node(html);
        String update = body.text("#tempc > div.detail-list-title > span.s > span");
        if (update != null) {
            Calendar calendar = Calendar.getInstance();
            if (update.contains("今天") || update.contains("分钟前")) {
                update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            } else if (update.contains("昨天")) {
                calendar.add(Calendar.DATE, -1);
                update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            } else if (update.contains("前天")) {
                calendar.add(Calendar.DATE, -2);
                update = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
            } else {
                String result = StringUtils.match("\\d+-\\d+-\\d+", update, 0);
                if (result == null) {
                    String[] rs = StringUtils.match("(\\d+)月(\\d+)号", update, 1, 2);
                    if (rs != null) {
                        result = calendar.get(Calendar.YEAR) + "-" + rs[0] + "-" + rs[1];
                    }
                }
                update = result;
            }
        }
        return update;
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new ArrayList<>();
        Node body = new Node(html);
        for (Node node : body.list("ul.mh-list > li > div.mh-item")) {
            String cid = node.hrefWithSplit("div > h2.title > a", 0);
            String title = node.text("div > h2.title > a");
            String cover = StringUtils.match("\\((.*?)\\)", node.attr("p.mh-cover", "style"), 1);
            String author = node.textWithSubstring("p.author", 3);
            // String update = node.text("p.zl"); 要解析好麻烦
            list.add(new Comic(TYPE, cid, title, cover, null, author));
        }
        return list;
    }

    @Override
    public Headers getHeader(String url) {
        String cid = "m".concat(StringUtils.match("cid=(\\d+)", url, 1));
        return Headers.of("Referer", "http://m.dm5.com/".concat(cid));
    }

    @Override
    public Headers getHeader(List<ImageUrl> list) {
        String cid = "";
        if (list != null) {
            cid = list.get(0).getChapter();
        }
        return Headers.of("Referer", "http://m.dm5.com/".concat(cid));
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            String path = args[CATEGORY_SUBJECT].concat(" ").concat(args[CATEGORY_AREA]).concat(" ").concat(args[CATEGORY_PROGRESS])
                    .concat(" ").concat(args[CATEGORY_ORDER]).trim();
            path = path.replaceAll("\\s+", "-");
            return StringUtils.format("http://www.dm5.com/manhua-list-%s-p%%d", path);
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("热血", "tag31"));
            list.add(Pair.create("恋爱", "tag26"));
            list.add(Pair.create("校园", "tag1"));
            list.add(Pair.create("百合", "tag3"));
            list.add(Pair.create("耽美", "tag27"));
            list.add(Pair.create("冒险", "tag2"));
            list.add(Pair.create("后宫", "tag8"));
            list.add(Pair.create("科幻", "tag25"));
            list.add(Pair.create("战争", "tag12"));
            list.add(Pair.create("悬疑", "tag17"));
            list.add(Pair.create("推理", "tag33"));
            list.add(Pair.create("搞笑", "tag37"));
            list.add(Pair.create("奇幻", "tag14"));
            list.add(Pair.create("魔法", "tag15"));
            list.add(Pair.create("恐怖", "tag29"));
            list.add(Pair.create("神鬼", "tag20"));
            list.add(Pair.create("历史", "tag4"));
            list.add(Pair.create("同人", "tag30"));
            list.add(Pair.create("运动", "tag34"));
            list.add(Pair.create("绅士", "tag36"));
            list.add(Pair.create("机战", "tag40"));
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
            list.add(Pair.create("人气", ""));
            list.add(Pair.create("新品上架", "s18"));
            return list;
        }

    }

}
