package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/26.
 */
public class EHentai extends MangaParser {

    public static final int TYPE = 60;
    public static final String DEFAULT_TITLE = "EHentai";

    public EHentai(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = "";
        if (page == 1)
            url = StringUtils.format("https://e-hentai.org/?f_doujinshi=1&f_manga=1&f_artistcg=1&f_gamecg=1&f_western=1&f_non-h=1&f_imageset=1&f_cosplay=1&f_asianporn=1&f_misc=1&f_search=%s&f_apply=Apply+Filter", keyword);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("div.ido > div > table.itg > tbody > tr[class^=gtr]")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSubString("td:eq(2) > div > div:eq(2) > a", 23, -2);
                String title = node.text("td:eq(2) > div > div:eq(2) > a");
                String cover = node.src("td:eq(2) > div > div:eq(0) > img");
                if (cover == null) {
                    String temp = node.textWithSubstring("td:eq(2) > div > div:eq(0)", 14).split("~", 2)[0];
                    cover = "http://ehgt.org/".concat(temp);
                }
                String update = node.textWithSubstring("td:eq(1)", 0, 10);
                String author = StringUtils.match("\\[(.*?)\\]", title, 1);
                title = title.replaceFirst("\\[.*?\\]\\s*", "");
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://g.e-hentai.org/g/%s", cid);
        return new Request.Builder().url(url).header("Cookie", "nw=1").build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String update = body.textWithSubstring("#gdd > table > tbody > tr:eq(0) > td:eq(1)", 0, 10);
        String title = body.text("#gn");
        String intro = body.text("#gj");
        String author = body.text("#taglist > table > tbody > tr > td:eq(1) > div > a[id^=ta_artist]");
//        String cover = body.href("#gdt > .gdtm > div > a");
        String cover = "https://github.com/Haleydu/Cimoc/raw/release-tci/screenshot/icon.png";
        comic.setInfo(title, cover, update, intro, author, true);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        String length = body.textWithSplit("#gdd > table > tbody > tr:eq(5) > td:eq(1)", " ", 0);
        int size = Integer.parseInt(length) % 40 == 0 ? Integer.parseInt(length) / 40 : Integer.parseInt(length) / 40 + 1;
        for (int i = 0; i != size; ++i) {
            list.add( new Chapter(Long.parseLong(sourceComic + "000" + i), sourceComic,"Ch" + i, String.valueOf(i)));
        }
        return list;
    }

//    @Override
//    public Request getRecentRequest(int page) {
//        String url = StringUtils.format("http://g.e-hentai.org/?page=%d", (page - 1));
//        return new Request.Builder().url(url).build();
//    }

//    @Override
//    public List<Comic> parseRecent(String html, int page) {
//        List<Comic> list = new LinkedList<>();
//        Node body = new Node(html);
//        for (Node node : body.list("table.itg > tbody > tr[class^=gtr]")) {
//            String cid = node.hrefWithSubString("td:eq(2) > div > div:eq(2) > a", 24, -2);
//            String title = node.text("td:eq(2) > div > div:eq(2) > a");
//            String cover = node.attr("td:eq(2) > div > div:eq(0) > img", "src");
//            if (cover == null) {
//                String temp = node.textWithSubstring("td:eq(2) > div > div:eq(0)", 14).split("~", 2)[0];
//                cover = "http://ehgt.org/".concat(temp);
//            }
//            String update = node.textWithSubstring("td:eq(1)", 0, 10);
//            String author = StringUtils.match("\\[(.*?)\\]", title, 1);
//            title = title.replaceFirst("\\[.*?\\]\\s*", "");
//            list.add(new Comic(TYPE, cid, title, cover, update, author));
//        }
//        return list;
//    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://g.e-hentai.org/g/%s/?p=%s", cid, path);
        return new Request.Builder().url(url).header("Cookie", "nw=1").build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        Node body = new Node(html);
        int count = 0;
        for (Node node : body.list("#gdt > div > div > a")) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + count);
            list.add(new ImageUrl(id, comicChapter, ++count, node.href(), true));
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder().url(url).build();
    }

    @Override
    public String parseLazy(String html, String url) {
        return new Node(html).src("a > img[style]");
    }

}
