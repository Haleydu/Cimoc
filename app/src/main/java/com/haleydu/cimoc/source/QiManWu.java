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
 * Created by Haleydu on 2020/8/20.
 * Committed by lx200916 on 2020/2/6：漫画源很不稳定，会随机抛出`网站升级中...`
 */

public class QiManWu extends MangaParser {

    public static final int TYPE = 53;
    public static final String DEFAULT_TITLE = "奇漫屋";
    public static final String baseUrl = "http://qiman6.com";

    public QiManWu(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page != 1) return null;
        String url = baseUrl + "/spotlight/?keyword=" + keyword;
        //url = "https://comic.mkzcdn.com/search/keyword/";
        RequestBody body = new FormBody.Builder()
                .add("keyword", keyword)
                .build();
        return new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1")
                .build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(".search-result > .comic-list-item")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.href("a");
                String title = node.text("p.comic-name");
                String cover = node.attr("img", "src");
                String author = node.text("p.comic-author");
                return new Comic(TYPE, cid, title, cover, null, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return baseUrl.concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(baseUrl));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseUrl.concat(cid);
        return new Request.Builder().url(url).build();
    }

    private static String ChapterHtml;

    @Override
    public Comic parseInfo(String html, Comic comic) {
        ChapterHtml = html;
        Node body = new Node(html);
        String update = body.text(".box-back2 > :eq(4)");
        if (!update.contains("更新时间：")) update = body.text(".box-back2 > :eq(3)");
        update = update.replace("更新时间：", "");
        String title = body.text(".box-back2 > h1");
        String intro = body.text("span.comic-intro");
        String author = body.text(".box-back2 > :eq(2)");
        String cover = body.src(".box-back1 > img");
        boolean status = isFinish(body.text(".box-back2 > p.txtItme.c1"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        String url = "http://qiman6.com/bookchapter/";
        String id = Objects.requireNonNull(StringUtils.match(" data: \\{ \"id\":(.*?),", html, 1)).trim();
        String id2 = Objects.requireNonNull(StringUtils.match(", \"id2\":(.*?)\\},", html, 1)).trim();
        RequestBody body = new FormBody.Builder().add("id", id).add("id2", id2).build();
        return new Request.Builder().url(url).post(body)
                .addHeader("Referer", baseUrl)
                .addHeader("Host", "qiman6.com")
                .build();
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        try {
            int k = 0;
            for (Node node : new Node(ChapterHtml).list("div.catalog-list > ul > li")) {
                String title = node.text("a");
                String path = node.attr("li", "data-id");
                list.add(new Chapter(Long.parseLong(sourceComic + "000" + k++), sourceComic, title, path));
            }
            JSONArray array = new JSONArray(html);
            for (int i = 0; i != array.length(); ++i) {
                JSONObject chapter = array.getJSONObject(i);
                String title = chapter.getString("name");
                String path = chapter.getString("id");
                list.add(new Chapter(Long.parseLong(sourceComic + "000" + k++), sourceComic, title, path));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://qiman6.com%s%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {

        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("eval\\((.*?\\}\\))\\)", html, 0);
        try {
            String str1 = DecryptionUtils.evalDecrypt(str, "newImgs");
            if (str1.isEmpty()) {
                str1 = DecryptionUtils.evalDecrypt(str);

            }
            String[] array = str1.split(",");
            for (int i = 0; i != array.length; ++i) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);
                list.add(new ImageUrl(id, comicChapter, i + 1, array[i], false));
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
        Node body = new Node(html);
        String update = body.text(".box-back2 > :eq(4)");
        if (!update.contains("更新时间：")) update = body.text(".box-back2 > :eq(3)");
        update = update.replace("更新时间：", "");
        return update;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1");
    }
}
