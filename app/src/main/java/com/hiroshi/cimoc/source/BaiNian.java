package com.hiroshi.cimoc.source;

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

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by ZhiWen on 2019/02/25.
 * fixed by haleydu on 2020/8/20.
 */

public class BaiNian extends MangaParser {

    public static final int TYPE = 13;
    public static final String DEFAULT_TITLE = "百年漫画";

    public BaiNian(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    // 这里一直无法弄好
    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = "https://m.bnmanhua.com/index.php/search.html";
        }

        RequestBody requestBodyPost = new FormBody.Builder()
                .add("keyword", keyword)
                .build();
        return new Request.Builder()
                .addHeader("Referer", "https://m.bnmanhua.com/")
                .addHeader("Host", "m.bnmanhua.com")
//                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:65.0) Gecko/20100101 Firefox/65.0")
                .url(url)
                .post(requestBodyPost)
                .build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("ul.tbox_m > li.vbox")) {
            @Override
            protected Comic parse(Node node) {
                String title = node.attr("a.vbox_t", "title");
                String cid = node.attr("a.vbox_t", "href");
                String cover = node.attr("a.vbox_t > mip-img", "src");
                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return "https://m.bnmanhua.com".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("m.bnmanhua.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://m.bnmanhua.com".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String cover = body.attr("div.dbox > div.img > mip-img", "src");
        String title = body.text("div.dbox > div.data > h4");
        String intro = body.text("div.tbox_js");
        String author = body.text("div.dbox > div.data > p.dir").substring(3).trim();
        String update = body.text("div.dbox > div.data > p.act").substring(3, 13).trim();
        boolean status = isFinish(body.text("span.list_item"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("div.tabs_block > ul > li > a")) {
            String title = node.text();
            String path = node.href();
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = "https://m.bnmanhua.com".concat(path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html,Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        String host = StringUtils.match("src=\"(.*?)\\/upload", html, 1);
        String path_str = StringUtils.match("z_img=\'\\[(.*?)\\]\'", html, 1);
        if (path_str != null && !path_str.equals("")) {
            try {
                String[] array = path_str.split(",");
                for (int i = 0; i != array.length; ++i) {
                    String path = array[i].replace("\"","").replace("\\","");
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    list.add(new ImageUrl(id,comicChapter,i + 1, host+"/"+path, false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        // 这里表示的是 parseInfo 的更新时间
        return new Node(html).text("div.dbox > div.data > p.act").substring(3, 13).trim();
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "https://m.bnmanhua.com");
    }
}
