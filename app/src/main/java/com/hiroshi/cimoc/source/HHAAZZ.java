package com.hiroshi.cimoc.source;

import android.util.Log;

import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.manager.PreferenceManager;
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

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/26.
 */
public class HHAAZZ extends MangaParser {

    public static final int TYPE = 2;
    public static final String DEFAULT_TITLE = "汗汗酷漫";
    public static String baseUrl = "";
    public static String sw = "";

    public HHAAZZ(Source source) {
        init(source, null);
        baseUrl = App.getPreferenceManager().getString(PreferenceManager.PREF_HHAAZZ_BASEURL, "");
        sw = App.getPreferenceManager().getString(PreferenceManager.PREF_HHAAZZ_SW, "");
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            final String url = baseUrl+"/comic/?act=search&st=".concat(keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("div.cComicList > li")) {
            @Override
            protected Comic parse(Node node) {
                final String cid = node.hrefWithSplit("a", 1);
                final String title = node.attr("a", "title");
                final String cover = node.src("a > img");
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return baseUrl+"/comic/".concat(cid);
    }

    @Override
    public Request getInfoRequest(String cid) {
        final String url = StringUtils.format(baseUrl+"/manhua/%s.html", cid);
        return new Request.Builder().url(url).build();
    }

    private String title = "";

    @Override
    public Comic parseInfo(String html, Comic comic) {
        final Node body = new Node(html);
        final String cover = body.src("#about_style > img");
        int index = 0;
        String update = "", intro = "", author = "";
        boolean status = false;
        for (Node node : body.list("#about_kit > ul > li")) {
            switch (index++) {
                case 0:
                    title = node.getChild("h1").text().trim();
                    break;
                case 1:
                    author = node.text().replace("作者:", "").trim();
                    break;
                case 2:
                    String test = node.text().replace("状态:", "").trim();
                    status = "连载" != test;
                    break;
                case 4:
                    update = node.text().replace("更新:", "").trim();
                    break;
                case 7:
                    intro = node.text().replace("简介", "").trim().substring(1);
                    break;
                default:
                    break;
            }
        }
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int i=0;
        for (Node node : body.list(".cVolList > ul")) {
            for (Node cnode : node.list("li")) {
                String title = cnode.attr("a", "title").replace(this.title, "").trim();
                String path = cnode.href("a");
                list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
            }
        }
        return list;
    }

    private String _path;

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = baseUrl+"".concat(path);
        _path = path;
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();

        //save page info
        final String pathId = Node.splitHref(_path, 0);
        final String pathS = Node.splitHref(_path, 4);

        Node body = new Node(html);
        int i = 1;
        for (Node node : body.list("#iPageHtm > a")) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            list.add(new ImageUrl(id, comicChapter, i,
                    StringUtils.format(baseUrl+"/%s/%d.html?s=%s&d=0", pathId, i, pathS),
                    true));

            i++;
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

        // get img key
        final String imgEleIds[] = {"img1021", "img2391", "img7652", "imgCurr"};
        String imgKey = null;
        for (int i = 0; i < imgEleIds.length; i++) {
            imgKey = body.attr("#".concat(imgEleIds[i]), "name");
            if (imgKey != null) break;
        }

        String[] servers = body.attr("#hdDomain", "value").split("\\|");

        //img key decode
        if (imgKey != null) {
            return servers[0] + unsuan(imgKey);
        }
        return null;
    }

    //https://stackoverflow.com/questions/2946067/what-is-the-java-equivalent-to-javascripts-string-fromcharcode
    public static String fromCharCode(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    private String unsuan(String s) {
        final String su = baseUrl.replace("http://","");
        boolean b = false;

        for (int i = 0; i < sw.split("|").length; i++) {
            if (su.indexOf(sw.split("|")[i]) > -1) {
                b = true;
                break;
            }
        }
        if (!b)
            return "";

        final String x = s.substring(s.length() - 1);
        final String w = "abcdefghijklmnopqrstuvwxyz";
        int xi = w.indexOf(x) + 1;
        final String sk = s.substring(s.length() - xi - 12, s.length() - xi - 1);
        s = s.substring(0, s.length() - xi - 12);
        String k = sk.substring(0, sk.length() - 1);
        String f = sk.substring(sk.length() - 1);

        for (int i = 0; i < k.length(); i++) {
            s = s.replace(k.substring(i, i + 1), Integer.toString(i));
        }
        String[] ss = s.split(f);
        s = "";
        for (int i = 0; i < ss.length; i++) {
            s += fromCharCode(Integer.parseInt(ss[i]));
        }
        return s;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).textWithSubstring("div.main > div > div.pic > div.con > p:eq(5)", 5);
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("li.clearfix > a.pic")) {
            String cid = node.hrefWithSplit(1);
            String title = node.text("div.con > h3");
            String cover = node.src("img");
            String update = node.textWithSubstring("div.con > p > span", 0, 10);
            String author = node.text("div.con > p:eq(1)");
            list.add(new Comic(TYPE, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", baseUrl);
    }
//
//    private static class Category extends MangaCategory {
//
//        @Override
//        public String getFormat(String... args) {
//            if (!"".equals(args[CATEGORY_SUBJECT])) {
//                return StringUtils.format("http://hhaass.com/lists/%s/%%d", args[CATEGORY_SUBJECT]);
//            } else if (!"".equals(args[CATEGORY_AREA])) {
//                return StringUtils.format("http://hhaass.com/lists/%s/%%d", args[CATEGORY_AREA]);
//            } else if (!"".equals(args[CATEGORY_READER])) {
//                return StringUtils.format("http://hhaass.com/duzhequn/%s/%%d", args[CATEGORY_PROGRESS]);
//            } else if (!"".equals(args[CATEGORY_PROGRESS])) {
//                return StringUtils.format("http://hhaass.com/lianwan/%s/%%d", args[CATEGORY_PROGRESS]);
//            } else {
//                return "http://hhaass.com/dfcomiclist_%d.htm";
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
//            list.add(Pair.create("图片", "14"));
//            list.add(Pair.create("冒险", "15"));
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
//            list.add(Pair.create("大陆", "19"));
//            list.add(Pair.create("香港", "20"));
//            list.add(Pair.create("欧美", "23"));
//            list.add(Pair.create("日文", "24"));
//            return list;
//        }
//
//        @Override
//        protected boolean hasReader() {
//            return true;
//        }
//
//        @Override
//        protected List<Pair<String, String>> getReader() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
//            list.add(Pair.create("少年", "1"));
//            list.add(Pair.create("少女", "2"));
//            list.add(Pair.create("青年", "3"));
//            return list;
//        }
//
//        @Override
//        protected boolean hasProgress() {
//            return true;
//        }
//
//        @Override
//        protected List<Pair<String, String>> getProgress() {
//            List<Pair<String, String>> list = new ArrayList<>();
//            list.add(Pair.create("全部", ""));
//            list.add(Pair.create("连载", "1"));
//            list.add(Pair.create("完结", "2"));
//            return list;
//        }
//
//    }

}

