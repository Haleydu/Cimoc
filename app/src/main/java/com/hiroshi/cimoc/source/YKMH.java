package com.hiroshi.cimoc.source;

import android.util.Log;

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

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Headers;
import okhttp3.Request;

public class YKMH extends MangaParser {
    public static final int TYPE = 91;
    public static final String DEFAULT_TITLE = "优酷漫画";
    public final String Host = "https://www.ykmh.com/";
    public final String mHost = "https://m.ykmh.com/";

    public YKMH(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        Log.d("SourceSearch:", String.valueOf(keyword));

        return new Request.Builder().url(mHost + "search/?keywords=" + keyword + "&page=" + page).addHeader("referer", "https://m.ykmh.com/search").addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36")
                .build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        Node body = new Node(html);
        return new NodeIterator(body.list("#update_list > div.UpdateList > div")) {
            @Override
            protected Comic parse(Node node) {
                Node titleN = node.getChild("div.itemTxt > a");
                String cid = titleN.hrefWithLastSplit();
                String title = titleN.text();
                String cover = node.attr("div.itemImg > a > img", "src");
                String Update = node.text("p.txtItme > span.date");
                String Author = node.text("p > a");
                return new Comic(TYPE, cid, title, cover, Update, Author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return StringUtils.format("%smanhua/%s", mHost, cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.ykmh.com", "/manhua/(\\w.+)/"));

    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "https://m.ykmh.com/search/", "user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36");
    }

    @Override
    public Request getInfoRequest(String cid) {
        Log.d("SourceInfo:", String.valueOf(cid));

        return new Request.Builder().url(mHost.concat("manhua/").concat(cid).concat("/")).addHeader("referer", "https://m.ykmh.com/search").addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36")
                .build();

    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        Node info = body.getChild("div.Introduct_Sub");
        String title = body.text("div#comicName");
        String cover = info.getChild("div#Cover > *").src();
        String update = info.text("p.txtItme > span.date");
        String author = info.getParent("p.txtItme > span.icon01").text();
        String intro = body.getParent("p#full-des #showmore-des").text();
        String isFinish = info.getParent("p.txtItme > span.icon01").text();
        boolean finish = false;
        if (isFinish.contains("完结")) finish = true;
        comic.setInfo(title, cover, update, intro, author, finish);


        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("div.chapter-warp ul.Drama > li > a")) {
            String title = node.text();
//            String path = StringUtils.split(node.href(), "/", 3);
            String path = node.hrefWithSubString(1);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) throws JSONException {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int i = 0;
        for (Node node : body.list("div.chapter-warp ul.Drama > li > a")) {
            String title = node.text();
//            String path = StringUtils.split(node.href(), "/", 3);
            String path = node.hrefWithSubString(1);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        Log.d("SourceImage:", String.valueOf(path));

        return new Request.Builder().url(mHost.concat(path)).addHeader("referer", "https://m.ykmh.com/search").addHeader("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.96 Safari/537.36")
                .build();
    }


    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) throws Manga.NetworkErrorException, JSONException {
        List<ImageUrl> list = new LinkedList<>();
        Matcher matcher = Pattern.compile("var chapterImages\\s*=\\s*\\[(.+?)]").matcher(html);
        if (!matcher.find()) {
            return null;
        }
        String CDATA = String.format("[%s]", matcher.group(1));
        JSONArray array = null;
        try {
            array = new JSONArray(CDATA);
            for (int i = 0; i < array.length(); i++) {
                String url = StringUtils.format("https://pic.w1fl.com%s", array.getString(i));
                Long comicChapter = chapter.getId();
                Long id = Long.parseLong(comicChapter + "000" + i + 1);
                list.add(new ImageUrl(id, comicChapter, i + 1, url, false));

            }

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("parseImages", "parseImages Error", e);
            return null;

        }

        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("p.txtItme > span.date");
    }

    @Override
    public String getTitle() {
        return DEFAULT_TITLE;
    }
}
