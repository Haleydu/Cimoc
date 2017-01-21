package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.manager.SourceManager;
import com.hiroshi.cimoc.parser.MangaCategory;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Pair;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/8.
 */
public class U17 extends MangaParser {

    public U17() {
        category = new Category();
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
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.comic_info > div.left > h1.fl");
        String cover = body.src("div.comic_info > div.left > div.coverBox > div.cover > a > img");
        String author = body.text("div.comic_info > div.right > div.author_info > div.info > a.name");
        String intro = body.text("#words");
        boolean status = isFinish(body.text("div.comic_info > div.left > div.info > div.top > div.line1 > span:eq(2)"));
        String update = body.textWithSubstring("div.main > div.chapterlist > div.chapterlist_box > div.bot > div.fl > span", 7);
        comic.setInfo(title, cover, update, intro, author, status);
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

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new ArrayList<>();
        Node body = new Node(html);
        String total = StringUtils.replaceAll(body.text("#comiclist > div > div.pagelist > em"), "\\D+", "");
        try {
            if (Integer.parseInt(total) < page) {
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (Node node : body.list("#comiclist > div > div.comiclist > ul > li")) {
            String cid = node.hrefWithSplit("div.info > h3 > strong > a", 1);
            String title = node.attr("div.info > h3 > strong > a", "title");
            String cover = node.src("div.cover > a > img");
            if (cover == null || cover.isEmpty()) {
                cover = node.attr("div.cover > a > img", "xsrc");
            }
            String author = node.text("div.info > h3 > a[title]");
            list.add(new Comic(SourceManager.SOURCE_U17, cid, title, cover, null, author));
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
            return StringUtils.format("http://www.u17.com/comic_list/%s_%s_ca99_%s_%s_ac0_as0_wm0_co99_ct99_p%%d.html",
                    args[0], args[2], args[4], args[5]);
        }

        @Override
        protected List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "th99"));
            list.add(Pair.create("搞笑", "th1"));
            list.add(Pair.create("魔幻", "th2"));
            list.add(Pair.create("生活", "th3"));
            list.add(Pair.create("恋爱", "th4"));
            list.add(Pair.create("动作", "th5"));
            list.add(Pair.create("科幻", "th6"));
            list.add(Pair.create("战争", "th7"));
            list.add(Pair.create("体育", "th8"));
            list.add(Pair.create("推理", "th9"));
            list.add(Pair.create("恐怖", "th11"));
            list.add(Pair.create("同人", "th12"));
            return list;
        }

        @Override
        protected boolean hasReader() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getReader() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", "gr99"));
            list.add(Pair.create("少年", "gr1"));
            list.add(Pair.create("少女", "gr2"));
            return list;
        }

        @Override
        protected boolean hasProgress() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getProgress() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("连载", "ss0"));
            list.add(Pair.create("完结", "ss1"));
            return list;
        }

        @Override
        protected boolean hasOrder() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getOrder() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("更新", "ob0"));
            list.add(Pair.create("人气", "ob9"));
            list.add(Pair.create("章节", "ob1"));
            list.add(Pair.create("评论", "ob3"));
            list.add(Pair.create("发布", "ob2"));
            list.add(Pair.create("收藏", "ob4"));
            return list;
        }

    }

}
