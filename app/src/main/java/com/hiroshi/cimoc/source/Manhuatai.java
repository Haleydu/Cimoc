package com.hiroshi.cimoc.source;



import android.util.Pair;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
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
import com.hiroshi.cimoc.soup.MDocument;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.ui.activity.ResultActivity;
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
        String url = StringUtils.format(baseUrl + "/getjson.shtml?d=1516243591359&q=%s",
                URLEncoder.encode(keyword, "UTF-8"), page);

        // 解决重复加载列表问题
        //
        // 思路：在新的一次请求（上拉加载）前检查新Url与上一次请求的是否一致。
        // 一致则返回空请求，达到阻断请求的目的；不一致则更新Map中存的Url，Map中不存在则新建
        if (url.equals(ResultActivity.searchUrls.get(TYPE))) {
            return null;
        } else {
            if (ResultActivity.searchUrls.get(TYPE) == null) {
                ResultActivity.searchUrls.append(TYPE, url);
            } else {
                ResultActivity.searchUrls.setValueAt(TYPE, url);
            }
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        JSONArray mangaDetailArray = new JSONArray(html);
        return new JsonIterator(mangaDetailArray) {
            @Override
            protected Comic parse(JSONObject object) throws JSONException {
                String title = object.getString("cartoon_name");
                String cid = object.getString("cartoon_id");
                String cover = null;
                String author = null;
                String update = object.getString("latest_cartoon_topic_name");
                try {
                    Node node = getComicNode(cid);
//                    cover = node.src("#offlinebtn-container > img");//搜索页解析封面,已失效
                    cover = node.dataUrl("#offlinebtn-container > img");//搜索页解析封面
                    author = node.text("div.jshtml > ul > li:nth-child(3)").substring(3);
                    update = node.text("div.jshtml > ul > li:nth-child(5)").substring(3);
                } catch (Manga.NetworkErrorException e) {
                    e.printStackTrace();
                }
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
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("div.jshtml > ul > li:nth-child(1)").substring(3);
//        String cover = body.src("#offlinebtn-container > img");//封面链接已改到style属性里了
        String cover = body.dataUrl("#offlinebtn-container > img");
//        Log.i("Cover", cover);
        String update = body.text("div.jshtml > ul > li:nth-child(5)").substring(3);
        String author = body.text("div.jshtml > ul > li:nth-child(3)").substring(3);
        String intro = body.text("div.wz.clearfix > div");
        boolean status = isFinish(body.text("div.jshtml > ul > li:nth-child(2)").substring(3));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("div.mhlistbody > ul > li > a")) {
            String title = node.attr("title");
//            String path = node.hrefWithSplit(0);//于2018.3失效
            String path = node.href();
//            Log.i("Path", path);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    //获取漫画图片Request
    @Override
    public Request getImagesRequest(String cid, String path) {
//        String url = StringUtils.format("http://m.manhuatai.com/%s/%s.html", cid, path);//于2018.3失效
        String url = StringUtils.format("https://m.manhuatai.com%s", path);
        return new Request.Builder().url(url).build();
    }


    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        MDocument document = new MDocument(html);
        String elScriptList = document.text("#comiclist > script:nth-child(2)");
        String str = StringUtils.match("<script>var mh_info=(\\{[^}]*\\}).*", elScriptList, 1);
        Gson gson = new Gson();
        MhInfo mh_info = gson.fromJson(str, MhInfo.class);
        int offset = mh_info.imgpath.charAt(1) - '%';
        ArrayList<Character> list1 = new ArrayList<Character>();
        for (int i = 0; i < mh_info.imgpath.length(); i++) {
            list1.add((char) (mh_info.imgpath.charAt(i) - offset));
        }
        String imgpath = org.apache.commons.lang3.StringUtils.join(list1, ",").replaceAll(",", "");
        String b = mh_info.comic_size;
        String c = "mhpic." + mh_info.domain;
        for (int i = 0; i < mh_info.totalimg; i++) {
            String d = (mh_info.startimg + i) + ".jpg" + b;
            String e = "https://" + c + "/comic/" + imgpath + d;
            list.add(new ImageUrl(i + 1, e, false));
        }
        return list;
    }


    class MhInfo {
        @SerializedName("startimg")
        int startimg;
        @SerializedName("totalimg")
        int totalimg;
        @SerializedName("pageid")
        int pageid;
        @SerializedName("comic_size")
        String comic_size;
        @SerializedName("domain")
        String domain;
        @SerializedName("imgpath")
        String imgpath;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.jshtml > ul > li:nth-child(5)").substring(3);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("a.sdiv")) {
            String cid = node.hrefWithSplit(0);
            String title = node.attr("title");
            String cover = node.getChild("img").attr("data-url");
//            String cover1 = node.attr("div > img", "data-url");
            Node node1 = null;
            try {
                node1 = getComicNode(cid);
            } catch (Manga.NetworkErrorException e) {
                e.printStackTrace();
            }
            if (StringUtils.isEmpty(cover) && node1 != null) {
//                cover = node.src("div > img");
                cover = node1.src("#offlinebtn-container > img");
            }
//            String update = node.text("div > span:nth-child(1)");
//            String author = "佚名";
//            String cover = null;
            String author = null;
            String update = null;
            if (node1 != null) {
//                cover = getComicNode(cid).src("#offlinebtn-container > img");
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
