package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by ZhiWen on 2019/02/25.
 * fix by haleydu on2020/8/16
 */

public class MHLove extends MangaParser {

    public static final int TYPE = 27;
    public static final String DEFAULT_TITLE = "漫画Love";
    public static final String baseUrl = "http://www.php06.com";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    public MHLove(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page != 1) {
            return null;
        }
        String url = StringUtils.format(baseUrl+"/statics/search.aspx?key=%s", keyword);
        return new Request.Builder()
                .addHeader("Referer", baseUrl)
                .url(url)
                .build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(".mh-search-result > ul > li")) {
            @Override
            protected Comic parse(Node node) {
                String cover = node.attr("img", "src");
                String title = node.text("h4").trim();
                String cid = node.attr(".mh-works-info > a", "href");
                String update = node.text(".mh-up-time.fr").replace("最后更新时间：","");
                return new Comic(TYPE, cid, title, cover, update, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseUrl + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String cover = body.src(".mh-date-bgpic > a > img");
        String intro = body.text("#workint > p");
        String title = body.attr(".mh-date-bgpic > a > img", "title");
        String update = body.text("div.mh-chapter-record > span > em");
        String author = body.text("span.one > em");
        // 连载状态
        boolean status = isFinish(body.text("p.works-info-tc > span:eq(3)"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("#mh-chapter-list-ol-0 > li > a")) {
            String title = node.text();
            String path = node.href();
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
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("qTcms_S_m_murl_e=\"(.*?)\"", html, 1);
        if (str != null) {
            try {
                str = DecryptionUtils.base64Decrypt(str);
                String[] array = str.split("\\$qingtiandy\\$");
                String preUrl = "";
                if(!array[0].contains("http")){
                    preUrl = "http://www.9qv.cn";
                }
                for (int i = 0; i != array.length; ++i) {
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    list.add(new ImageUrl(id, comicChapter, i + 1, preUrl + array[i], false));
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
        // 这里表示的是更新时间
        return new Node(html).text("div.mh-chapter-record > span > em");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", baseUrl);
    }

}
