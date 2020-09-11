package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import okhttp3.Headers;
import okhttp3.Request;

import static com.hiroshi.cimoc.core.Manga.getResponseBody;

/**
 * Created by Haleydu on 2020/9/8.
 */

public class QiMiaoMH extends MangaParser {

    public static final int TYPE = 56;
    public static final String DEFAULT_TITLE = "奇妙漫画";
    private static final String baseUrl = "https://www.qimiaomh.com";

    public QiMiaoMH(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = "";
        if (page == 1) {
            url = StringUtils.format(baseUrl+"/action/Search?keyword=%s", keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(".mt20 > .classification")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.href("h2 > a");
                String title = node.text("h2 > a");
                String cover = node.attr("a > img", "data-src");
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return baseUrl+cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(baseUrl));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseUrl + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.attr(".ctdbLeft > a","title");
        String cover = body.attr(".ctdbLeft > a > img","src");
        String intro = body.text("#worksDesc").trim();
        String author = body.text(".detailBox > .author").replace("作者","").trim();
        String update = body.text(".updeteStatus > .date").replace("更新：","").trim();
        String statusStr = body.text("a.status");
        boolean status = isFinish(statusStr);
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list(".comic-content-list > ul.comic-content-c")) {
            String title = node.attr("li.cimg > a","title");
            String path = node.href("li.cimg > a");
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = baseUrl + path;
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) throws Manga.NetworkErrorException {
        List<ImageUrl> list = new LinkedList<>();
        try {
            String did = Objects.requireNonNull(StringUtils.match("var did =(.*?);", html, 1)).trim();
            String sid = Objects.requireNonNull(StringUtils.match("var sid =(.*?);", html, 1)).trim();
            float random = new Random().nextFloat();
            String url = StringUtils.format(baseUrl + "/Action/Play/AjaxLoadImgUrl?did=%s&sid=%s&tmp=%f", did, sid, random);
            Request request = new Request.Builder().url(url).build();
            String body = getResponseBody(App.getHttpClient(), request);

            JSONArray jsonArray = new JSONObject(body).getJSONArray("listImg");
            for (int i = 0; i < jsonArray.length(); ++i) {
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i);
                list.add(new ImageUrl(id, comicChapter, i + 1, jsonArray.getString(i), false));
            }
        } catch (JSONException e) {
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
        return new Node(html).text(".updeteStatus > .date").replace("更新：","").trim();
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", baseUrl);
    }


}

