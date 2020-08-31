package com.hiroshi.cimoc.source;

import android.util.Pair;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaCategory;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/10/1.
 */

public class HHSSEE {//extends MangaParser {
//
//    public static final int TYPE = 7;
//    public static final String DEFAULT_TITLE = "汗汗漫画";
//
//    public HHSSEE(Source source) {
//        init(source, new Category());
//    }
//
//    public static Source getDefaultSource() {
//        return new Source(null, DEFAULT_TITLE, TYPE, true);
//    }
//
//    @Override
//    public Request getSearchRequest(String keyword, int page) {
//        if (page == 1) {
//            String url = "http://www.hhmmoo.com/comic/?act=search&st=".concat(keyword);
//            return new Request.Builder().url(url).build();
//        }
//        return null;
//    }
//
//    @Override
//    public SearchIterator getSearchIterator(String html, int page) {
//        Node body = new Node(html);
//        return new NodeIterator(body.list("#list > div.cComicList > li > a")) {
//            @Override
//            protected Comic parse(Node node) {
//                String cid = node.hrefWithSubString(7, -6);
//                String title = node.text();
//                String cover = node.src("img");
//                return new Comic(TYPE, cid, title, cover, null, null);
//            }
//        };
//    }
//
//    @Override
//    public String getUrl(String cid) {
//        return StringUtils.format("http://www.hhmmoo.com/manhua%s.html", cid);
//    }
//
//    @Override
//    public Request getInfoRequest(String cid) {
//        String url = StringUtils.format("http://www.hhmmoo.com/manhua%s.html", cid);
//        return new Request.Builder().url(url).build();
//    }
//
//    @Override
//    public Comic parseInfo(String html, Comic comic) {
//        Node body = new Node(html);
//        String title = body.text("#about_kit > ul > li:eq(0) > h1");
//        String cover = body.src("#about_style > img");
//        String update = body.textWithSubstring("#about_kit > ul > li:eq(4)", 3);
//        if (update != null) {
//            String[] args = update.split("\\D");
//            update = StringUtils.format("%4d-%02d-%02d", Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
//        }
//        String author = body.textWithSubstring("#about_kit > ul > li:eq(1)", 3);
//        String intro = body.textWithSubstring("#about_kit > ul > li:eq(7)", 3);
//        boolean status = isFinish(body.text("#about_kit > ul > li:eq(2)"));
//        comic.setInfo(title, cover, update, intro, author, status);
//        return comic;
//    }
//
//    @Override
//    public List<Chapter> parseChapter(String html, Comic comic) {
//        List<Chapter> list = new ArrayList<>();
//        Node body = new Node(html);
//        String name = body.text("#about_kit > ul > li:eq(0) > h1");
//        int i=0;
//        for (Node node : body.list("#permalink > div.cVolList > ul.cVolUl > li > a")) {
//            Long sourceComic=null;
//            if (comic.getId() == null) {
//                sourceComic = Long.parseLong(comic.getSource() + sourceToComic + "00");
//            } else {
//                sourceComic = Long.parseLong(comic.getSource() + sourceToComic + comic.getId());
//            }
//            Long id = Long.parseLong(sourceComic+"000"+i);
//
//            String title = node.text();
//            title = title.replaceFirst(name, "").trim();
//            String[] array = StringUtils.match("/page(\\d+).*s=(\\d+)", node.attr("href"), 1, 2);
//            //String path = array != null ? array[0].concat(" ").concat(array[1]) : "";
//            String path = array != null ? array[0].concat("-").concat(array[1]) : "";
//            list.add(new Chapter(id, sourceComic, title.trim(), path));
//            i++;
//        }
//        return list;
//    }
//
//    @Override
//    public Request getImagesRequest(String cid, String path) {
//        String[] array = path.split("-");
//        String url = StringUtils.format("http://www.hhmmoo.com/page%s/1.html?s=%s", array[0], array[1]);
//        return new Request.Builder().url(url).build();
//    }
//
//    @Override
//    public List<ImageUrl> parseImages(String html, Chapter chapter) {
//        List<ImageUrl> list = new ArrayList<>();
//        Node body = new Node(html);
//        int page = Integer.parseInt(body.attr("#hdPageCount", "value"));
//        String path = body.attr("#hdVolID", "value");
//        String server = body.attr("#hdS", "value");
//        for (int i = 1; i <= page; ++i) {
//            Long comicChapter = chapter.getId();
//            Long id = Long.parseLong(comicChapter + "000" + i);
//            list.add(new ImageUrl(id, comicChapter, i, StringUtils.format("http://www.hhmmoo.com/page%s/%d.html?s=%s", path, i, server), true));
//        }
//        return list;
//    }
//
//    @Override
//    public Request getLazyRequest(String url) {
//        return new Request.Builder().url(url).build();
//    }
//
//    @Override
//    public String parseLazy(String html, String url) {
//        Node body = new Node(html);
//        String server = body.attr("#hdDomain", "value");
//        if (server != null) {
//            server = server.split("\\|")[0];
//            String name = body.attr("#iBodyQ > img", "name");
//            String result = unsuan(name).substring(1);
//            return server.concat(result);
//        }
//        return null;
//    }
//
//    @Override
//    public Request getCheckRequest(String cid) {
//        return getInfoRequest(cid);
//    }
//
//    @Override
//    public String parseCheck(String html) {
//        String update = new Node(html).textWithSubstring("#about_kit > ul > li:eq(4)", 3);
//        if (update != null) {
//            String[] args = update.split("\\D");
//            update = StringUtils.format("%4d-%02d-%02d", Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
//        }
//        return update;
//    }
//
//    private String unsuan(String str) {
//        int num = str.length() - str.charAt(str.length() - 1) + 'a';
//        String code = str.substring(num - 13, num - 2);
//        String cut = code.substring(code.length() - 1);
//        str = str.substring(0, num - 13);
//        code = code.substring(0, code.length() - 1);
//        for (int i = 0; i < code.length(); i++) {
//            str = str.replace(code.charAt(i), (char) ('0' + i));
//        }
//        StringBuilder builder = new StringBuilder();
//        String[] array = str.split(cut);
//        for (int i = 0; i != array.length; ++i) {
//            builder.append((char) Integer.parseInt(array[i]));
//        }
//        return builder.toString();
//    }
//
//    @Override
//    public List<Comic> parseCategory(String html, int page) {
//        List<Comic> list = new ArrayList<>();
//        Node body = new Node(html);
//        for (Node node : body.list("#list > div.cComicList > li > a")) {
//            String cid = node.hrefWithSubString(7, -6);
//            String title = node.attr("title");
//            String cover = node.src("img");
//            list.add(new Comic(TYPE, cid, title, cover, null, null));
//        }
//        return list;
//    }
//
//    @Override
//    public Headers getHeader() {
//        return Headers.of("Referer", "http://www.hhmmoo.com");
//    }
//
//    private static class Category extends MangaCategory {
//        @Override
//        public String getFormat(String... args) {
//            if (!"".equals(args[CATEGORY_SUBJECT])) {
//                return StringUtils.format("http://www.hhmmoo.com/comic/class_%s/%%d.html", args[CATEGORY_SUBJECT]);
//            } else if (!"".equals(args[CATEGORY_AREA])) {
//                return StringUtils.format("http://www.hhmmoo.com/comic/class_%s/%%d.html", args[CATEGORY_AREA]);
//            } else {
//                return "http://www.hhmmoo.com/comic/%d.html";
//            }
//        }
//
//        @Override
//        protected List<Pair<String, String>> getSubject() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
//            list.add(Pair.create("萌系", "1"));
//            list.add(Pair.create("搞笑", "2"));
//            list.add(Pair.create("格斗", "3"));
//            list.add(Pair.create("科幻", "4"));
//            list.add(Pair.create("剧情", "5"));
//            list.add(Pair.create("侦探", "6"));
//            list.add(Pair.create("竞技", "7"));
//            list.add(Pair.create("魔法", "8"));
//            list.add(Pair.create("神鬼", "9"));
//            list.add(Pair.create("校园", "10"));
//            list.add(Pair.create("惊栗", "11"));
//            list.add(Pair.create("厨艺", "12"));
//            list.add(Pair.create("伪娘", "13"));
//            list.add(Pair.create("冒险", "15"));
//            list.add(Pair.create("小说", "19"));
//            list.add(Pair.create("耽美", "21"));
//            list.add(Pair.create("经典", "22"));
//            list.add(Pair.create("亲情", "25"));
//            return list;
//        }
//
//        @Override
//        protected boolean hasArea() {
//            return true;
//        }
//
//        @Override
//        protected List<Pair<String, String>> getArea() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
//            list.add(Pair.create("香港", "20"));
//            list.add(Pair.create("欧美", "23"));
//            list.add(Pair.create("日文", "24"));
//            return list;
//        }
//
//    }

}
