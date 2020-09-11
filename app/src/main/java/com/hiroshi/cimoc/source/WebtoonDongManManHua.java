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
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

import static com.hiroshi.cimoc.core.Manga.getResponseBody;

/**
 * Created by Haleydu on 2020/08/07.
 */

public class WebtoonDongManManHua extends MangaParser {

    public static final int TYPE = 11;
    public static final String DEFAULT_TITLE = "咚漫漫画";
    public static final String baseUrl = "https://www.dongmanmanhua.cn";

    public WebtoonDongManManHua(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = baseUrl + "/search/?keyword=" + keyword;
            return new Request.Builder().url(url).addHeader("Referer", "www.dongmanmanhua.cn").build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(".card_wrap.search > .card_lst > li > a")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(-1);
                String title = node.text("div.info > p.subj");
                String cover = node.src("img");
                String author = node.text("div.info > p.author");
                return new Comic(TYPE, cid, title, cover, null, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return baseUrl + "/episodeList?titleNo=".concat(cid);
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseUrl + "/episodeList?titleNo=".concat(cid);
        return new Request.Builder().url(url).addHeader("Referer", "www.dongmanmanhua.cn").build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        k=0;
        Node body = new Node(html);
        String title = body.text(".detail_header > .info > h1.subj");
        String cover = body.src("ul#_listUl > li:eq(0) > a > span.thmb > img");
        String update = body.text("ul#_listUl > li:eq(0) > a > span.date").trim();
        String author = body.text(".detail_header > div.info > span.author");
        String intro = body.text("#_asideDetail > p.summary");
        boolean status = isFinish(body.text("#_asideDetail > p.day_info"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    private int k=0;
    public List<Chapter> parseChapter(Node body, Long sourceComic){
        List<Chapter> list = new LinkedList<>();
        for (Node node : body.list("ul#_listUl > li > a")) {
            String title = node.text("span.subj > span")+" "+node.text("span.tx");
            String path = "https:" + node.href();
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + k++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        //下次再优化吧，这样写比较容易但是效率低。
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node nodePage : body.list("div.detail_lst > div.paginate > a")) {
            String urlPage = nodePage.href();
            String urlPageTag = nodePage.attr("a","class");
            if (urlPage.equals("#") && (urlPageTag==null || urlPageTag.equals(""))){
                list.addAll(parseChapter(body, sourceComic));
            }else if (urlPageTag==null || urlPageTag.equals("")){
                try {
                    String pageTagUrl = baseUrl + urlPage;
                    Request request = new Request.Builder()
                            .url(pageTagUrl)
                            .addHeader("Referer", "www.dongmanmanhua.cn")
                            .build();
                    String htmlPage = getResponseBody(App.getHttpClient(), request);
                    list.addAll(parseChapter(new Node(htmlPage), sourceComic));
                } catch (Manga.NetworkErrorException e) {
                    e.printStackTrace();
                }
            }else if (urlPageTag.equals("pg_next")){
                try {
                    String pageTagUrl = baseUrl + urlPage;
                    Request request = new Request.Builder()
                            .url(pageTagUrl)
                            .addHeader("Referer", "www.dongmanmanhua.cn")
                            .build();
                    String htmlPageNext = getResponseBody(App.getHttpClient(), request);
                    list.addAll(parseChapter(htmlPageNext,comic,sourceComic));
                } catch (Manga.NetworkErrorException e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        return new Request.Builder().url(path).addHeader("Referer", "www.dongmanmanhua.cn").build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();

        Node body = new Node(html);
        int i = 1;
        for (Node node : body.list("div#_imageList > img")) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            String url = node.attr("img", "data-url");
            list.add(new ImageUrl(id, comicChapter, i++, url, false));
        }
        if (!list.isEmpty()) return list;

        String docUrl = StringUtils.match("documentURL:.*?'(.*?)'", html, 0);
        String motiontoonPath = StringUtils.match("jpg:.*?'(.*?)\\{", html, 0);
        try {
            if (docUrl == null) return list;
            String html1 = getResponseBody(App.getHttpClient(),
                    new Request.Builder().url(docUrl)
                            .addHeader("Referer", "www.dongmanmanhua.cn")
                            .build()
            );
            JSONObject motiontoonJson = new JSONObject(html1).getJSONObject("assets").getJSONObject("image");
            Iterator<String> Json_Iterator = motiontoonJson.keys();
            i = 1;
            while (Json_Iterator.hasNext()) {
                String key = Json_Iterator.next();
                if (key.contains("layer")) {
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    list.add(new ImageUrl(id, comicChapter, i++, motiontoonPath + motiontoonJson.getString(key), false));
                }
            }
        } catch (Manga.NetworkErrorException e) {
            e.printStackTrace();
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
        return new Node(html).text("ul#_listUl > li:eq(0) > a > span.date").trim();
    }

    @Override
    public Request getCategoryRequest(String format, int page) {
        if (page == 1) {
            return new Request.Builder().url(format).addHeader("Referer", "https://m.webtoons.com").build();
        }
        return null;
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new ArrayList<>();
        Node body = new Node(html);
        for (Node node : body.list("#ct > ul > li > a")) {
            String cid = node.hrefWithSplit(-1);
            String title = node.text("div.info > p.subj > span");
            String cover = node.attrWithSplit("div.pic", "style", "\\(|\\)", 1);
            list.add(new Comic(TYPE, cid, title, cover, null, null));
        }
        return list;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", baseUrl);
    }
}
