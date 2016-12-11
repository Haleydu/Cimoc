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
 * Created by Hiroshi on 2016/12/9.
 */

public class Jmydm extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = "http://www.iibq.com/search/?keyword=".concat(keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#form1 > .cPubBody > .cPubBack > .cDataList > .cInfoItem")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(".cListTitle > a", 1);
                String title = node.text(".cListTitle > a");
                title = StringUtils.match("(.*)\\[.*\\]$", title, 1);
                String cover = node.src(".cListSlt > img");
                String update = node.text(".cInfoTime");
                update = StringUtils.split(update, " ", -2);
                String author = node.text(".cListh1 > .cl1_2");
                if (author != null) {
                    author = author.substring(3);
                }
                return new Comic(SourceManager.SOURCE_JMYDM, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://www.iibq.com/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.titleNav > ul > li > h1");
        String cover = body.src("#detailsBox > div.comicCover > img");
        String update = StringUtils.substring(body.text("#detailsBox > div.comicInfo > ul > li:eq(3)"), 5);
        String author = body.text("#p_profile > a");
        String intro = body.text("#p_profile");
        intro = intro.replaceFirst(author, "    ");
        intro = StringUtils.split(intro, "\\s{4,}", -1);
        boolean status = isFinish(StringUtils.substring(body.text("#detailsBox > div.comicInfo > ul > li:eq(2)"), 3));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        Node body = new Node(html);
        List<Chapter> list = new LinkedList<>();
        String name = body.text("div.titleNav > ul > li > h1");
        for (Node node : body.list(".comicBox > .relativeRec > .cVol > .cVolList > div > a")) {
            String title = node.text();
            title = title.replaceFirst(name, "");
            String temp = StringUtils.match("\\d+(-\\d+)?[集话卷]$", title, 0);
            if (temp != null) {
                title = temp;
            }
            String path = StringUtils.substring(node.hrefWithSplit(2), 9);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://www.iibq.com/comic/%s/viewcomic%s", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        if (server != null) {
            String[] str = StringUtils.match("sFiles=\"(.*?)\";var sPath=\"(.*?)\"", html, 1, 2);
            if (str != null) {
                String[] result = unsuan(str[0]);
                for (int i = 0; i != result.length; ++i) {
                    list.add(new ImageUrl(i + 1, buildUrl(str[1].concat(result[i])), false));
                }
            }
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        if (page == 1) {
            String url = "http://www.iibq.com/comicupdate";
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("div.cTopItem1")) {
            String cid = node.hrefWithSplit("div:eq(2) > a", 1);
            String title = node.text("div:eq(2) > a");
            title = StringUtils.match("(.*)\\[.*\\]$", title, 1);
            String cover = node.src("a:eq(1) > img");
            String update = StringUtils.substring(node.text("div:eq(4)"), 3);
            String author = node.text("div:eq(3)");
            list.add(new Comic(SourceManager.SOURCE_JMYDM, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return StringUtils.substring(new Node(html).text("#detailsBox > div.comicInfo > ul > li:eq(3)"), 5);
    }

    private String[] unsuan(String str) {
        int num = str.length() - str.charAt(str.length() - 1) + 'a';
        String code = str.substring(num - 13, num - 3);
        String cut = str.substring(num - 3, num - 2);
        str = str.substring(0, num - 13);
        for (int i = 0; i < 10; ++i) {
            str = str.replace(code.charAt(i), (char) ('0' + i));
        }
        StringBuilder builder = new StringBuilder();
        String[] array = str.split(cut);
        for (int i = 0; i != array.length; ++i) {
            builder.append((char) Integer.parseInt(array[i]));
        }
        return builder.toString().split("\\|");
    }

}
