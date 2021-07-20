package com.haleydu.cimoc.source;

import com.haleydu.cimoc.model.Chapter;
import com.haleydu.cimoc.model.Comic;
import com.haleydu.cimoc.model.ImageUrl;
import com.haleydu.cimoc.model.Source;
import com.haleydu.cimoc.parser.MangaParser;
import com.haleydu.cimoc.parser.NodeIterator;
import com.haleydu.cimoc.parser.SearchIterator;
import com.haleydu.cimoc.parser.UrlFilter;
import com.haleydu.cimoc.soup.Node;
import com.haleydu.cimoc.utils.DecryptionUtils;
import com.haleydu.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by ZhiWen on 2019/02/25.
 */

public class SixMH extends MangaParser {

    public static final int TYPE = 101;
    public static final String DEFAULT_TITLE = "6漫画";
    private static final String website = "www.sixmh6.com";
    private static String ChapterHtml;

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public SixMH(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("http://%s/search.php?keyword=%s", website, keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public String getUrl(String cid) {
        return "http://".concat(website).concat("/").concat(cid).concat("/");
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(website, "\\w+", 0));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("div.cy_list_mh > ul")) {
            @Override
            protected Comic parse(Node node) {
                String title = node.text("li.title > a");
                String cid = node.href("li > a.pic");
                String cover = node.attr("a.pic > img", "src");
                String update = node.text("li.updata > a > span");

                return new Comic(TYPE, cid, title, cover, update, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://" + website  + cid ;
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        ChapterHtml = html;
        Node body = new Node(html);
        String cover = body.src("img.pic");
        String intro = body.text("p#comic-description");
        String title = body.text("div.cy_title > h1");
        String update = body.text("div.cy_zhangjie_top > p >font");
        String author = body.text("div.cy_xinxi > span:eq(0)");

        // 连载状态
        boolean status = isFinish(body.text("div.cy_xinxi > span:eq(1) > a"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        String url = "http://"+website+"/bookchapter/";
        Node body = new Node(html);
        String id = body.attr("a#zhankai","data-id");
        String id2 =body.attr("a#zhankai","data-vid");

        RequestBody requestBody = new FormBody.Builder().add("id", id).add("id2", id2).build();
        return new Request.Builder().url(url).post(requestBody).build();
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        try {
            int i = 0;
            for (Node node : new Node(ChapterHtml).list("div.cy_plist > ul > li")) {
                String title = node.text();
                String path = node.hrefWithSplit("a", 1);
                list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
            }
            JSONArray array = new JSONArray(html);
            for (int k = 0; k != array.length(); ++k) {
                JSONObject chapter = array.getJSONObject(k);
                String title = chapter.getString("chaptername");
                String path = chapter.getString("chapterid");
                list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://%s%s%s.html", website, cid,path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();

        String str = StringUtils.match("eval\\(.*\\)", html, 0);
        str = DecryptionUtils.evalDecrypt(str, "newImgs");

        int i = 0;
        for (String url : Objects.requireNonNull(str).split(",")) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            list.add(new ImageUrl(id, comicChapter, ++i, url, false));
        }

        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.cy_zhangjie_top > p >font");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://"+website);
    }

}