package com.hiroshi.cimoc.source;

import com.google.common.collect.Lists;
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

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by WinterWhisper on 2019/2/25.
 */
public class CCMH extends MangaParser {

    public static final int TYPE = 23;
    public static final String DEFAULT_TITLE = "CC漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public CCMH(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = "";
        if (page == 1) {
            url = "http://m.ccmh6.com/Search";

            RequestBody requestBodyPost = new FormBody.Builder()
                    .add("Key", keyword)
                    .build();

            return new Request.Builder()
                    .addHeader("Referer", "http://m.ccmh6.com/Search")
                    .addHeader("Origin", "http://m.ccmh6.com")
                    .addHeader("Host", "m.ccmh6.com")
                    .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/12.0 Mobile/15A372 Safari/604.1")
                    .url(url)
                    .post(requestBodyPost)
                    .build();

        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(".list > div")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit("a", 1);
                String title = node.textWithSplit("a", "\\s+", 0);
                String cover = node.src("a > img");
//                if (cover.startsWith("//")) cover = "https:" + cover;
//                String update = node.text(".itemTxt > p.txtItme:eq(3)");
//                boolean finish = node.textWithSplit("a","\\s+",1) == "完结";
                String author = node.textWithSplit("a", "\\s+", 2);
                return new Comic(TYPE, cid, title, cover, "", author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return StringUtils.format("http://m.ccmh6.com/manhua/%s", cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.50mh.com", "manhua\\/(\\w+)", 1));
    }


    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://m.ccmh6.com/manhua/%s", cid);
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/12.0 Mobile/15A372 Safari/604.1")
                .url(url)
                .build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String intro = body.text(".intro");
        String title = body.text(".other > div > strong");
        String cover = body.src(".cover > img");
//        if (cover.startsWith("//")) cover = "https:" + cover;
        String author = body.textWithSplit(".other", "\\s+|：", 8);
        String update = body.textWithSplit(".other", "\\s+|：", 12)
                .replace("[", "").replace("]", "");
        boolean status = isFinish(body.textWithSplit(".other", "\\s+|：", 10));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int i=0;
        for (Node node : body.list(".list > a")) {
            String title = node.attr("title");
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }

        return Lists.reverse(list);
    }

    private String _cid, _path;

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.ccmh6.com/manhua/%s/%s.html", cid, path);
        _cid = cid;
        _path = path;
        return new Request.Builder()
                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/12.0 Mobile/15A372 Safari/604.1")
                .url(url)
                .build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();

        //find image count
        Matcher pageCountMatcher = Pattern.compile("<a href=\"\\?p=(\\d+)\">\\d+<\\/a>").matcher(html);
        int pageCount = 0;
        while (pageCountMatcher.find()) {
            final int pageCountTemp = Integer.parseInt(pageCountMatcher.group(1));
            pageCount = pageCount > pageCountTemp ? pageCount : pageCountTemp;
        }

        for (int i = 0; i < pageCount; i++) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            list.add(new ImageUrl(id, comicChapter, i+1, StringUtils.format("http://m.ccmh6.com/manhua/%s/%s.html?p=%d", _cid, _path, i + 1), true));
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder()
                .addHeader("Referer", StringUtils.format("http://m.ccmh6.com/manhua/%s/%s.html", _cid, _path))
                .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 7.0;) Chrome/58.0.3029.110 Mobile")
                .url(url).build();
    }

    @Override
    public String parseLazy(String html, String url) {
        Node body = new Node(html);
        String src = body.src(".img > img");
        return src;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text(".Introduct_Sub > .sub_r > .txtItme:eq(4)");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.ccmh6.com/");
    }

}
