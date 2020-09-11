package com.hiroshi.cimoc.source;

import android.util.Pair;


import com.google.common.collect.Lists;
import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.core.Manga;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;


/**
 * Created by reborn on 18-1-18.
 */

public class Manhuatai extends MangaParser {

    public static final int TYPE = 49;
    public static final String DEFAULT_TITLE = "漫画台";
    public static final String baseUrl = "https://m.manhuatai.com";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public Manhuatai(Source source) {
        init(source, new Category());
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = StringUtils.format(baseUrl + "/api/getsortlist/?product_id=2&productname=mht&platformname=wap&orderby=click&search_key=%s&page=%d&size=48",
                URLEncoder.encode(keyword, "UTF-8"), page);

        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        JSONObject object = new JSONObject(html);

        return new JsonIterator(object.getJSONObject("data").getJSONArray("data")) {
            @Override
            protected Comic parse(JSONObject object) throws JSONException {
                String title = object.getString("comic_name");
                String cid = object.getString("comic_newid");
                String cover = "https://image.yqmh.com/mh/" + object.getString("comic_id") + ".jpg-300x400.webp";
                String author = null;
                String update = null;
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    private Node getComicNode(String cid) throws Manga.NetworkErrorException {
        Request request = getInfoRequest(cid);
        String html = Manga.getResponseBody(App.getHttpClient(), request);
        return new Node(html);
    }

//    private String getResponseBody(OkHttpClient client, Request request) throws Manga.NetworkErrorException {
//        Response response = null;
//        try {
//            response = client.newCall(request).execute();
//            if (response.isSuccessful()) {
////                return response.body().string();
//
//                // 1.修正gb2312编码网页读取错误
//                byte[] bodybytes = response.body().bytes();
//                String body = new String(bodybytes);
//                if (body.indexOf("charset=gb2312") != -1) {
//                    body = new String(bodybytes, "GB2312");
//                }
//                return body;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (response != null) {
//                response.close();
//            }
//        }
//        throw new Manga.NetworkErrorException();
//    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://www.manhuatai.com/".concat(cid) + "/";
        return new Request.Builder().url(url).build();
    }

    //获取封面等信息（非搜索页）
    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.attr("h1#detail-title", "title");
        String cover = body.attr("div.detail-cover > img", "data-src");
        cover = "https:" + cover;
        String update = body.text("span.update").substring(0,10);
        String author = null;
        String intro = body.text("div#js_comciDesc > p.desc-content");
        comic.setInfo(title, cover, update, intro, author, false);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("ol#j_chapter_list > li > a")) {
            String title = node.attr( "title");
            String path = node.hrefWithSplit(1);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return Lists.reverse(list);
    }

    private String _path = null;

    //获取漫画图片Request
    @Override
    public Request getImagesRequest(String cid, String path) {
        _path = path;
        String url = StringUtils.format("https://m.manhuatai.com/api/getcomicinfo_body?product_id=2&productname=mht&platformname=wap&comic_newid=%s", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter)  {
        List<ImageUrl> list = new LinkedList<>();
        try {
            JSONObject object = new JSONObject(html);
            if (object.getInt("status") != 0) {
                return list;
            }

            JSONArray chapters = object.getJSONObject("data").getJSONArray("comic_chapter");
            JSONObject chapterNew = null;
            for (int i = 0; i < chapters.length(); i++) {
                chapterNew = chapters.getJSONObject(i);
                String a = chapterNew.getString("chapter_id");
                if(a.equals(_path)) {
                    break;
                }
            }

            String ImagePattern = "http://mhpic." + chapterNew.getString("chapter_domain") + chapterNew.getString("rule") + "-mht.low.webp";

            for (int index = chapterNew.getInt("start_num"); index <= chapterNew.getInt("end_num"); index++) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + index);

                String image = ImagePattern.replaceFirst("\\$\\$", Integer.toString(index));
                list.add(new ImageUrl(id, comicChapter, index, image, false));
            }
        } catch (JSONException ex) {
            // ignore
        }

        return list;
    }
//
//
//    class MhInfo {
//        @SerializedName("startimg")
//        int startimg;
//        @SerializedName("totalimg")
//        int totalimg;
//        @SerializedName("pageid")
//        int pageid;
//        @SerializedName("comic_size")
//        String comic_size;
//        @SerializedName("domain")
//        String domain;
//        @SerializedName("imgpath")
//        String imgpath;
//    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("span.update").substring(0,10);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("a.sdiv")) {
            String cid = node.hrefWithSplit(0);
            String title = node.attr("title");
            String cover = node.getChild("img").attr("data-url");
            Node node1 = null;
            try {
                node1 = getComicNode(cid);
            } catch (Manga.NetworkErrorException e) {
                e.printStackTrace();
            }
            if (StringUtils.isEmpty(cover) && node1 != null) {
                cover = node1.src("#offlinebtn-container > img");
            }
            String author = null;
            String update = null;
            if (node1 != null) {
                author = node1.text("div.jshtml > ul > li:nth-child(3)").substring(3);
                update = node1.text("div.jshtml > ul > li:nth-child(5)").substring(3);
            }
            list.add(new Comic(TYPE, cid, title, cover, update, author));
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
            return StringUtils.format("https://www.manhuatai.com/%s_p%%d.html",
                    args[CATEGORY_SUBJECT]);
        }

        @Override
        public List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部漫画", "all"));
            list.add(Pair.create("知音漫客", "zhiyinmanke"));
            list.add(Pair.create("神漫", "shenman"));
            list.add(Pair.create("风炫漫画", "fengxuanmanhua"));
            list.add(Pair.create("漫画周刊", "manhuazhoukan"));
            list.add(Pair.create("飒漫乐画", "samanlehua"));
            list.add(Pair.create("飒漫画", "samanhua"));
            list.add(Pair.create("漫画世界", "manhuashijie"));
//            list.add(Pair.create("排行榜", "top"));

//            list.add(Pair.create("热血", "rexue"));
//            list.add(Pair.create("神魔", "shenmo"));
//            list.add(Pair.create("竞技", "jingji"));
//            list.add(Pair.create("恋爱", "lianai"));
//            list.add(Pair.create("霸总", "bazong"));
//            list.add(Pair.create("玄幻", "xuanhuan"));
//            list.add(Pair.create("穿越", "chuanyue"));
//            list.add(Pair.create("搞笑", "gaoxiao"));
//            list.add(Pair.create("冒险", "maoxian"));
//            list.add(Pair.create("萝莉", "luoli"));
//            list.add(Pair.create("武侠", "wuxia"));
//            list.add(Pair.create("社会", "shehui"));
//            list.add(Pair.create("都市", "dushi"));
//            list.add(Pair.create("漫改", "mangai"));
//            list.add(Pair.create("杂志", "zazhi"));
//            list.add(Pair.create("悬疑", "xuanyi"));
//            list.add(Pair.create("恐怖", "kongbu"));
//            list.add(Pair.create("生活", "shenghuo"));
            return list;
        }

        @Override
        protected boolean hasOrder() {
            return false;
        }

        @Override
        protected List<Pair<String, String>> getOrder() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("更新", "update"));
//            list.add(Pair.create("发布", "index"));
//            list.add(Pair.create("人气", "view"));
            return null;
        }

    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "https://m.manhuatai.com");
    }

}
