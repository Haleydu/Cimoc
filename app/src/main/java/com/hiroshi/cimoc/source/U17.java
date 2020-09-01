package com.hiroshi.cimoc.source;

import android.util.Pair;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaCategory;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.CipherSuite;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by Hiroshi on 2016/8/8.
 */
public class U17 extends MangaParser {

    public static final int TYPE = 4;
    public static final String DEFAULT_TITLE = "有妖气";

    public U17(Source source) {
        init(source, new Category());
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

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
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return StringUtils.format("http://www.u17.com/comic/%s.html", cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.u17.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://www.u17.com/comic/%s.html", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.comic_info > div.left > h1.fl");
        String cover = body.src("div.comic_info > div.left > div.coverBox > div.cover > a > img");
        String author = body.text("div.comic_info > div.right > div.author_info > div.info > a.name");
        String intro = body.text("#words");
        boolean status = isFinish(body.text("div.comic_info > div.left > div.info > div.top > div.line1 > span:eq(2)"));
        String update = body.textWithSubstring("div.main > div.chapterlist > div.chapterlist_box > div.bot > div.fl > span", 7);
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        // http://m.u17.com/chapter/list?comicId=%s
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int i=0;
        for (Node node : body.list("#chapter > li > a")) {
            String title = node.text();
            String path = node.hrefWithSplit(1);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        // String url = StringUtils.format("http://www.u17.com/chapter/%s.html", path);
        String url = "http://www.u17.com/comic/ajax.php?mod=chapter&act=get_chapter_v5&chapter_id=".concat(path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        /*
        String result = StringUtils.match("image_list: .*?\\('(.*?)'\\)", html, 1);
        try {
            JSONObject object = new JSONObject(result);
            for (int i = 1; i <= object.length(); ++i) {
                String str = object.getJSONObject(String.valueOf(i)).getString("src");
                list.add(new ImageUrl(i, DecryptionUtils.base64Decrypt(str), false));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        try {
            JSONObject object = new JSONObject(html);
            JSONArray array = object.getJSONArray("image_list");
            for (int i = 0; i < array.length(); ++i) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);
                String url = array.getJSONObject(i).getString("src");
                list.add(new ImageUrl(id, comicChapter, i + 1, url, false));
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

    @Override
    public Request getCategoryRequest(String format, int page) {
        String[] args = format.split(" ");
        String url = "http://www.u17.com/comic/ajax.php?mod=comic_list&act=comic_list_new_fun&a=get_comic_list";
        RequestBody body = new FormBody.Builder()
                .add("data[group_id]", args[0])
                .add("data[theme_id]", args[1])
                .add("data[is_vip]", "no")
                .add("data[accredit]", "no")
                .add("data[color]", "no")
                .add("data[comic_type]", "no")
                .add("data[series_status]", args[2])
                .add("data[order]", args[3])
                .add("data[page_num]", String.valueOf(page))
                .add("data[read_mode]", "no")
                .build();
        return new Request.Builder().url(url).post(body).addHeader("Referer", "http://www.u17.com").build();
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new ArrayList<>();
        try {
            JSONArray array = new JSONObject(html).getJSONArray("comic_list");
            for (int i = 0; i < array.length(); ++i) {
                JSONObject object = array.getJSONObject(i);
                String cid = object.getString("comic_id");
                String title = object.getString("name");
                String cover = object.getString("cover");
                list.add(new Comic(TYPE, cid, title, cover, null, null));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://www.u17.com");
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            return StringUtils.format("%s %s %s %s",
                    args[CATEGORY_READER], args[CATEGORY_SUBJECT], args[CATEGORY_PROGRESS], args[CATEGORY_ORDER]);
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "no"));
            list.add(Pair.create("搞笑", "1"));
            list.add(Pair.create("魔幻", "2"));
            list.add(Pair.create("生活", "3"));
            list.add(Pair.create("恋爱", "4"));
            list.add(Pair.create("动作", "5"));
            list.add(Pair.create("科幻", "6"));
            list.add(Pair.create("战争", "7"));
            list.add(Pair.create("体育", "8"));
            list.add(Pair.create("推理", "9"));
            list.add(Pair.create("惊悚", "11"));
            list.add(Pair.create("同人", "12"));
            return list;
        }

        @Override
        protected boolean hasReader() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getReader() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "no"));
            list.add(Pair.create("少年", "1"));
            list.add(Pair.create("少女", "2"));
            list.add(Pair.create("耽美", "3"));
            list.add(Pair.create("绘本", "4"));
            return list;
        }

        @Override
        protected boolean hasProgress() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getProgress() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "no"));
            list.add(Pair.create("连载", "0"));
            list.add(Pair.create("完结", "1"));
            return list;
        }

        @Override
        protected boolean hasOrder() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全站最热", "0"));
            list.add(Pair.create("更新时间", "1"));
            list.add(Pair.create("上升最快", "2"));
            list.add(Pair.create("最新发布", "3"));
            return list;
        }

    }

}
