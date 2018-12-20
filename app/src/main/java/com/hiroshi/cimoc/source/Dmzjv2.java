package com.hiroshi.cimoc.source;

import android.util.Pair;

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
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class Dmzjv2 extends MangaParser {

    public static final int TYPE = 10;
    public static final String DEFAULT_TITLE = "动漫之家v2";

//    private List<UrlFilter> filter = new ArrayList<>();

    public Dmzjv2(Source source) {
        init(source, new Category());
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
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = StringUtils.format("https://m.dmzj.com/search/%s.html", keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        try {
            //var serchArry=[{"id":27445,"name":"\u5982\u679c\u6709\u59b9\u59b9\u5c31\u597d\u4e86@comic","comic_py":"ruguoyoumeimeijiuhaole","alias_name":"\u8981\u662f\u6709\u4e2a\u59b9\u59b9\u5c31\u597d\u4e86","authors":"\u5e73\u5742\u8bfb\/\u3044\uff5e\u3069\u3045\uff5e","types":"\u7231\u60c5\/\u540e\u5bab\/\u8f7b\u5c0f\u8bf4","zone":"\u65e5\u672c","status":"\u8fde\u8f7d\u4e2d","last_update_chapter_name":"\u7b2c24\u8bdd","last_update_chapter_id":75090,"hot_hits":16106154,"last_updatetime":1521125443,"description":"\u75af\u72c2\u7684\u5c0f\u8bf4\u5bb6\u7fbd\u5c9b\u4f0a\u6708\uff0c\u662f\u65e5\u591c\u4e3a\u4e86\u521b\u9020\u51fa\u672a\u66fe\u8c0b\u9762\u7684\u59b9\u59b9\u5f62\u8c61\u800c\u594b\u6597\u7684\u73b0\u4ee3\u76ae\u683c\u9a6c\u5229\u7fc1\u3002\u4ed6\u7684\u8eab\u8fb9\u805a\u96c6\u4e86\u4f5c\u5bb6\u3001\u753b\u5e08\u3001\u7f16\u8f91\u3001\u7a0e\u52a1\u4eba\u7b49\u5145\u6ee1\u4e2a\u6027\u7684\u602a\u4eba\uff1a\u7231\u4e0e\u624d\u80fd\u90fd\u662f\u91cd\u91cf\u7ea7\u7684\u6781\u81f4\u6b8b\u5ff5\u7cfb\u7f8e\u5c11\u5973\u53ef\u513f\u90a3\u7531\u591a\uff0c\u70e6\u607c\u7740\u604b\u7231\u70e6\u607c\u7740\u53cb\u60c5\u70e6\u607c\u7740\u68a6\u60f3\u7684\u9752\u6625\u4e09\u51a0\u738b\u767d\u5ddd\u4eac\uff0c\u80f8\u6000\u5927\u5fd7\u7684\u5e05\u54e5\u738b\u5b50\u4e0d\u7834\u6625\u6597\uff0c\u8f7b\u89c6\u4eba\u751f\u7684\u5929\u624d\u63d2\u753b\u5e08\u60e0\u90a3\u5239\u90a3\uff0c\u867d\u7136\u5f88\u53ef\u9760\u4f46\u662f\u5374\u4e0d\u60f3\u4f9d\u9760\u4ed6\u7684\u9b3c\u755c\u7a0e\u6b3e\u5b88\u62a4\u4eba\u5927\u91ce\u963f\u4ec0\u5229\uff0c\u5185\u5fc3\u9634\u6697\u7684\u7f16\u8f91\u571f\u5c90\u5065\u6b21\u90ce...","cover":"webpic\/19\/ruguoyoumeimei03029.jpg"},{"id":41016,"name":"\u5982\u679c\u6709\u59b9\u59b9\u5c31\u597d\u4e86\u5916\u4f20","comic_py":"ruguoyoumeimeijiuhaolewaizhuan","alias_name":"\u8981\u662f\u6709\u4e2a\u59b9\u59b9\u5c31\u597d\u4e86,\u53ea\u8981\u6210\u4e3a\u59b9\u59b9\u5c31\u597d\u4e86","authors":"\u5e73\u5742\u8bfb\/\u30b3\u30d0\u30b7\u30b3","types":"\u7231\u60c5\/\u540e\u5bab\/\u8f7b\u5c0f\u8bf4","zone":"\u65e5\u672c","status":"\u8fde\u8f7d\u4e2d","last_update_chapter_name":"\u7b2c02\u8bdd","last_update_chapter_id":66979,"hot_hits":1864533,"last_updatetime":1503305259,"description":"\u8981\u662f\u80fd\u88ab\u5f53\u6210\u59b9\u59b9\u5c31\u597d\u4e86\uff01\u6210\u4e3a\u59b9\u59b9\u5927\u4f5c\u6218\u3002","cover":"webpic\/5\/ysygmmjhlwz6486l.jpg"},{"id":8031,"name":"\u66fe\u7ecf_\u5982\u679c\u6709\u5e86\u795d\u65e5\u7684\u8bdd","comic_py":"zjrgyqzrdh","alias_name":"","authors":"\u30b9\u30bf\u30b8\u30aa\u30cb\u30ca\u30ca","types":"\u751f\u6d3b\/\u6821\u56ed","zone":"\u65e5\u672c","status":"\u8fde\u8f7d\u4e2d","last_update_chapter_name":"\u5168\u4e00\u8bdd","last_update_chapter_id":15129,"hot_hits":4175,"last_updatetime":1323068912,"description":"\u5982\u679c\u4f60\u65e9\u8d77\u53d1\u73b0\u65c1\u8fb9\u8eba\u7740\u4e00\u4e2a\u4eba\uff0c\u90a3\u4eba\u5c45\u7136\u8bf4\u5c31\u662f\u672a\u6765\u7684\u4f60\uff01\u59d0\u5e73\u9759\u7684\u751f\u6d3b\u5c31\u6b64\u65e0\u6cd5\u6de1\u5b9a.","cover":"webpic\/18\/ruguoyoude.jpg"},{"id":19239,"name":"\u5149\u955c\u00b7poker","comic_py":"guangjingpoker","alias_name":"","authors":"\u5982\u679c\u6709\u5982\u679cwuyu","types":"\u5192\u9669","zone":"\u5185\u5730","status":"\u8fde\u8f7d\u4e2d","last_update_chapter_name":"\u7b2c01\u8bdd \u660e\u4e89\u6697\u6597","last_update_chapter_id":38205,"hot_hits":274,"last_updatetime":1434533642,"description":"\u5149\u955c\u00b7poker","cover":"img\/webpic\/12\/1002082321434533480.jpg"}]
            String str = StringUtils.match("serchArry=(\\[.*?\\])", html, 1);
            return new JsonIterator(new JSONArray(str)) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        String cid = object.getString("id");
                        String title = object.getString("name");
                        String cover = "https://images.dmzj.com/".concat(object.getString("cover"));
                        String author = object.optString("authors");
                        return new Comic(TYPE, cid, title, cover, null, author);
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
    public String getUrl(String cid) {
        return StringUtils.format("http://m.dmzj.com/info/%s.html", cid);
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://m.dmzj.com/info/%s.html", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String intro = body.textWithSubstring("p.txtDesc", 3);
        String title = body.attr("#Cover > img", "title");
        String cover = body.src("#Cover > img");
        String author = body.text("div.Introduct_Sub > div.sub_r > p:eq(0) > a");
        String update = body.textWithSubstring("div.Introduct_Sub > div.sub_r > p:eq(3) > span.date", 0, 10);
        boolean status = isFinish(body.text("div.Introduct_Sub > div.sub_r > p:eq(2) > a:eq(3)"));
        comic.setInfo(title, cover, update, intro, author, status);
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
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).textWithSubstring("div.Introduct_Sub > div.sub_r > p:eq(3) > span.date", 0, 10);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        try {
            JSONArray array = new JSONArray(html);
            for (int i = 0; i != array.length(); ++i) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    String cid = object.getString("id");
                    String title = object.getString("title");
                    String cover = object.getString("cover");
                    Long time = object.has("last_updatetime") ? object.getLong("last_updatetime") * 1000 : null;
                    String update = time == null ? null : new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(time));
                    String author = object.optString("authors");
                    list.add(new Comic(TYPE, cid, title, cover, update, author));
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
    public Headers getHeader() {
        return Headers.of("Referer", "http://images.dmzj.com/");
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            String path = args[CATEGORY_SUBJECT].concat(" ").concat(args[CATEGORY_READER]).concat(" ").concat(args[CATEGORY_PROGRESS])
                    .concat(" ").concat(args[CATEGORY_AREA]).trim();
            if (path.isEmpty()) {
                path = String.valueOf(0);
            } else {
                path = path.replaceAll("\\s+", "-");
            }
            return StringUtils.format("http://v2.api.dmzj.com/classify/%s/%s/%%d.json", path, args[CATEGORY_ORDER]);
        }

        @Override
        public List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("冒险", "4"));
            list.add(Pair.create("百合", "3243"));
            list.add(Pair.create("生活", "3242"));
            list.add(Pair.create("四格", "17"));
            list.add(Pair.create("伪娘", "3244"));
            list.add(Pair.create("悬疑", "3245"));
            list.add(Pair.create("后宫", "3249"));
            list.add(Pair.create("热血", "3248"));
            list.add(Pair.create("耽美", "3246"));
            list.add(Pair.create("其他", "16"));
            list.add(Pair.create("恐怖", "14"));
            list.add(Pair.create("科幻", "7"));
            list.add(Pair.create("格斗", "6"));
            list.add(Pair.create("欢乐向", "5"));
            list.add(Pair.create("爱情", "8"));
            list.add(Pair.create("侦探", "9"));
            list.add(Pair.create("校园", "13"));
            list.add(Pair.create("神鬼", "12"));
            list.add(Pair.create("魔法", "11"));
            list.add(Pair.create("竞技", "10"));
            list.add(Pair.create("历史", "3250"));
            list.add(Pair.create("战争", "3251"));
            list.add(Pair.create("魔幻", "5806"));
            list.add(Pair.create("扶她", "5345"));
            list.add(Pair.create("东方", "5077"));
            list.add(Pair.create("奇幻", "5848"));
            list.add(Pair.create("轻小说", "6316"));
            list.add(Pair.create("仙侠", "7900"));
            list.add(Pair.create("搞笑", "7568"));
            list.add(Pair.create("颜艺", "6437"));
            list.add(Pair.create("性转换", "4518"));
            list.add(Pair.create("高清单行", "4459"));
            list.add(Pair.create("治愈", "3254"));
            list.add(Pair.create("宅系", "3253"));
            list.add(Pair.create("萌系", "3252"));
            list.add(Pair.create("励志", "3255"));
            list.add(Pair.create("节操", "6219"));
            list.add(Pair.create("职场", "3328"));
            list.add(Pair.create("西方魔幻", "3365"));
            list.add(Pair.create("音乐舞蹈", "3326"));
            list.add(Pair.create("机战", "3325"));
            return list;
        }

        @Override
        public boolean hasArea() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getArea() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("日本", "2304"));
            list.add(Pair.create("韩国", "2305"));
            list.add(Pair.create("欧美", "2306"));
            list.add(Pair.create("港台", "2307"));
            list.add(Pair.create("内地", "2308"));
            list.add(Pair.create("其他", "8453"));
            return list;
        }

        @Override
        public boolean hasReader() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getReader() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("少年", "3262"));
            list.add(Pair.create("少女", "3263"));
            list.add(Pair.create("青年", "3264"));
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
            list.add(Pair.create("连载", "2309"));
            list.add(Pair.create("完结", "2310"));
            return list;
        }

        @Override
        public boolean hasOrder() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("更新", "1"));
            list.add(Pair.create("人气", "0"));
            return list;
        }

    }

}
