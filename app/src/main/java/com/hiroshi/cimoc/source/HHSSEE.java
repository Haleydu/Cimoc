package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.core.parser.NodeIterator;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/10/1.
 */

public class HHSSEE extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = "http://www.hhssee.com/comic/?act=search&st=".concat(keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#list > div.cComicList > li > a")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.attr("href");
                cid = cid.substring(7, cid.length() - 5);
                String title = node.text();
                String cover = node.attr("img", "src");
                return new Comic(SourceManager.SOURCE_HHSSEE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("http://www.hhssee.com/manhua%s.html", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public String parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("#about_kit > ul > li:eq(0) > h1").trim();
        String cover = body.attr("#about_style > img", "src");
        String[] args = body.text("#about_kit > ul > li:eq(4)", 3).split("\\D");
        String update = StringUtils.format("%4d-%02d-%02d",
                Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        String author = body.text("#about_kit > ul > li:eq(1)", 3);
        String intro = body.text("#about_kit > ul > li:eq(7)", 3);
        boolean status = body.text("#about_kit > ul > li:eq(2)").contains("完结");
        comic.setInfo(title, cover, update, intro, author, status);

        return null;
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        String name = body.text("#about_kit > ul > li:eq(0) > h1").trim();
        for (Node node : body.list("#permalink > div.cVolList > ul.cVolUl > li > a")) {
            String title = node.text();
            title = title.replaceFirst(name, "");
            String temp = StringUtils.match("\\d+(-\\d+)?[集话卷]$", title, 0);
            if (temp != null) {
                title = temp;
            }
            String[] array = StringUtils.match("/page(\\d+).*s=(\\d+)", node.attr("href"), 1, 2);
            String path = array != null ? array[0].concat(" ").concat(array[1]) : "";
            list.add(new Chapter(title.trim(), path));
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        if (page == 1) {
            String url = "http://www.hhssee.com/top/newrating.aspx";
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#list > div.cTopComicList > div.cComicItem")) {
            String cid = node.attr("div.cListSlt > a", "href");
            cid = cid.substring(7, cid.length() - 5);
            String title = node.text("a > span.cComicTitle");
            String cover = node.attr("div.cListSlt > a > img", "src");
            String[] args = node.text("span.cComicRating", 5).split("\\D");
            String update = StringUtils.format("%4d-%02d-%02d",
                    Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
            String author = node.text("span.cComicAuthor");
            list.add(new Comic(SourceManager.SOURCE_HHSSEE, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String[] array = path.split(" ");
        String url = StringUtils.format("http://www.hhssee.com/page%s/1.html?s=%s", array[0], array[1]);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        Node body = new Node(html);
        int page = Integer.parseInt(body.attr("#hdPageCount", "value"));
        String path = body.attr("#hdVolID", "value");
        String server = body.attr("#hdS", "value");
        for (int i = 1; i <= page; ++i) {
            list.add(new ImageUrl(i, StringUtils.format("http://www.hhssee.com/page%s/%d.html?s=%s", path, i, server), true));
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder().url(url).build();
    }

    @Override
    public String parseLazy(String html, String url) {
        Node body = new Node(html);
        String server = body.attr("#hdDomain", "value").split("\\|")[0];
        String name = body.attr("#iBodyQ > img", "name");
        String result = unsuan(name).substring(1);
        return server.concat(result);
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        String[] args = new Node(html).text("span.cComicRating", 5).split("\\D");
        return StringUtils.format("%4d-%02d-%02d", Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    private String unsuan(String str) {
        int num = str.length() - str.charAt(str.length() - 1) + 'a';
        String code = str.substring(num - 13, num - 2);
        String cut = code.substring(code.length() - 1);
        str = str.substring(0, num - 13);
        code = code.substring(0, code.length() - 1);
        for (int i = 0; i < code.length(); i++) {
            str = str.replace(code.charAt(i), (char) ('0' + i));
        }
        StringBuilder builder = new StringBuilder();
        String[] array = str.split(cut);
        for (int i = 0; i != array.length; ++i) {
            builder.append((char) Integer.parseInt(array[i]));
        }
        return builder.toString();
    }

}
