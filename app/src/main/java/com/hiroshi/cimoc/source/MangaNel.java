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
import com.hiroshi.cimoc.soup.Node;
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
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by nich on 2017/8/6.
 */

public class MangaNel extends MangaParser {

    public static final int TYPE = 43;
    public static final String DEFAULT_TITLE = "MangaNel";
    
    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }
    
    public MangaNel(Source source) {
        init(source, new Category());
    }
    
    /**
     * 搜索页的 HTTP 请求
     *
     * @param keyword 关键字
     * @param page    页码
     */
    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = "http://manganelo.com/home/getjson_searchstory";
    
        RequestBody formBody = new FormBody.Builder()
            .add("searchword", keyword)
            .add("search_style", "tentruyen")
            .build();
        
        return new Request.Builder().url(url).post(formBody).build();
    }
    
    /**
     * 获取搜索结果迭代器，这里不直接解析成列表是为了多图源搜索时，不同图源漫画穿插的效果
     *
     * @param html 页面源代码
     * @param page 页码，可能对于一些判断有用
     */
    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        if (page > 1) return null; // MangaNel的搜索不支持分页，最多仅返回20个搜索结果
        
        try {
            return new JsonIterator(new JSONArray(html)) {
                @Override
                protected Comic parse(JSONObject object) {
                    try {
                        String cid = object.getString("nameunsigned");
                        String title = object.getString("name");
                        if (title.contains("<span")){
                            Node n = new Node(title);
                            title = n.text();
                        }
                        String cover = object.getString("image");
                        String update = object.getString("lastchapter");
                        String author = object.getString("author");
                        return new Comic(TYPE, cid, title, cover, update, author);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 详情页的 HTTP 请求
     *
     * @param cid 漫画 ID
     */
    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://manganelo.com/manga/" + cid;
        return new Request.Builder().url(url).build();
    }
    
    /**
     * 解析详情
     *
     * @param html  页面源代码
     * @param comic 漫画实体类，需要设置其中的字段
     */
    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.manga-info-top > ul.manga-info-text > li > h1");
        String cover = body.src("div.manga-info-pic > img");
        String update = body.list("div.manga-info-top >  ul.manga-info-text > li")
            .get(3)
            .text()
            .replace("Last updated : ", "");
        String author = body.list("div.manga-info-top >  ul.manga-info-text > li").get(1).text("a");
        String intro = body.text("#noidungm");
        boolean status = isFinish(body.list("div.manga-info-top >  ul.manga-info-text > li")
            .get(2)
            .text());
        comic.setInfo(title, cover, update, intro, author, status);
    }
    
    /**
     * 解析章节列表
     *
     * @param html 页面源代码
     */
    @Override
    public List<Chapter> parseChapter(String html) {
        Set<Chapter> set = new LinkedHashSet<>();
        Node body = new Node(html);
        for (Node node : body.list("div.chapter-list > div.row")) {
            String title = node.text("span > a");
            String path = node.list("span > a").get(0).href();
            set.add(new Chapter(title, path));
        }
        return new LinkedList<>(set);
    }
    
    /**
     * 图片列表的 HTTP 请求
     *
     * @param cid  漫画 ID
     * @param path 章节路径
     */
    @Override
    public Request getImagesRequest(String cid, String path) {
        return new Request.Builder().url(path).build();
    }
    
    /**
     * 解析图片列表，若为惰性加载，则 {@link ImageUrl#lazy} 为 true
     * 惰性加载的情况，一次性不能拿到所有图片链接，例如网站使用了多次异步请求 {@link DM5#parseImages}，或需要跳转到不同页面
     * 才能获取 {@link HHSSEE#parseImages}，这些情况一般可以根据页码构造出相应的请求链接，到阅读时再解析
     * 支持多个链接 ，例如 {@link IKanman#parseImages}
     *
     * @param html 页面源代码
     */
    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        Node body = new Node(html);
        int i = 0;
        for (Node node : body.list("div.vung-doc > img")) {
            list.add(new ImageUrl(++i, node.src(), false));
        }
        return list;
    }
    
    /**
     * 获取下载图片时的 HTTP 请求头，一般用来设置 Referer 和 Cookie
     */
    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://manganelo.com/");
    }
    
    @Override
    public Request getCategoryRequest(String format, int page) {
        return super.getCategoryRequest(format, page);
    }
    
    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        int total = Integer.parseInt(StringUtils.match("\\d+", body.list("div.phan-trang > a")
            .get(4)
            .href(), 0));
        if (page <= total) {
            for (Node node : body.list("div.truyen-list > div.list-truyen-item-wrap")) {
                String cid = node.href("h3 > a").replace("http://manganelo.com/manga/", "");
                String title = node.text("h3 > a");
                String cover = node.src("a > img");
                String update = node.list("a").get(2).text();
                String author = node.text("div:eq(1) > div:eq(1) > dl:eq(1) > dd > a");
                list.add(new Comic(TYPE, cid, title, cover, update, author));
            }
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
            String path = "category=".concat(args[CATEGORY_SUBJECT])
                .concat("&alpha=all&state=")
                .concat(args[CATEGORY_PROGRESS])
                .concat("&group=all")
                .trim();
            path = path.replaceAll("\\s+", "-");
            return StringUtils.format("http://manganel.com/manga_list?type=new&page=%%d&%s", path);
        }
        
        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("All", "all"));
            list.add(Pair.create("Action", "2"));
//            list.add(Pair.create("Adult", "3"));
            list.add(Pair.create("Adventure", "4"));
            list.add(Pair.create("Comedy", "6"));
            list.add(Pair.create("Cooking", "7"));
//            list.add(Pair.create("Donjinshi", "9"));
            list.add(Pair.create("Drama", "10"));
//            list.add(Pair.create("Ecchi", "11"));
            list.add(Pair.create("Fantasy", "12"));
//            list.add(Pair.create("Gender bender", "13"));
//            list.add(Pair.create("Harem", "14"));
            list.add(Pair.create("Historical", "15"));
            list.add(Pair.create("Horror", "16"));
            list.add(Pair.create("Josei", "17"));
            list.add(Pair.create("Manhua", "44"));
            list.add(Pair.create("Manhwa", "43"));
            list.add(Pair.create("Martial Arts", "19"));
            list.add(Pair.create("Mature", "20"));
            list.add(Pair.create("Mecha", "21"));
            list.add(Pair.create("Medical", "22"));
            list.add(Pair.create("Mystery", "24"));
            list.add(Pair.create("One Shot", "25"));
            list.add(Pair.create("Psychological", "26"));
            list.add(Pair.create("Romance", "27"));
            list.add(Pair.create("Sci Fi", "29"));
            return list;
        }
        
        @Override
        protected boolean hasArea() {
            return false;
        }
        
        @Override
        protected List<Pair<String, String>> getArea() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("All", ""));
            return list;
        }
        
        @Override
        public boolean hasProgress() {
            return true;
        }
        
        @Override
        public List<Pair<String, String>> getProgress() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("All", "all"));
            list.add(Pair.create("Ongoing", "Ongoing"));
            list.add(Pair.create("Completed", "Completed"));
            return list;
        }
        
        @Override
        protected boolean hasOrder() {
            return false;
        }
        
        @Override
        protected List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("All", ""));
            return list;
        }
        
    }
}
