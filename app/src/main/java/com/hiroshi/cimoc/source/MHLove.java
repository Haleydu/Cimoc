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
 */

public class MHLove extends MangaParser {

    public static final int TYPE = 23;
    public static final String DEFAULT_TITLE = "漫画Love";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public MHLove(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("http://m.manhualove.com/statics/search.aspx?key=%s",
                    URLEncoder.encode(keyword, "UTF-8"));
        }
        return new Request.Builder()
                .addHeader("Referer", "http://m.manhualove.com/")
                .addHeader("Host", "m.manhualove.com")
//                .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 12_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/12.0 Mobile/15A372 Safari/604.1")
                .url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#listbody > li")) {
            @Override
            protected Comic parse(Node node) {

                String cover = node.attr("a:eq(0) > img", "src");

                String title = node.text("a:eq(1)");
                String cid = node.attr("a:eq(0)", "href");
                cid = cid.substring(1, cid.length() - 1);

                return new Comic(TYPE, cid, title, cover, null, null);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.manhualove.com/".concat(cid) + "/";
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String cover = body.src("#Cover > img");
        String intro = body.text("div.Introduct > p:eq(1)");
        String title = body.attr("#Cover > img", "title");

        String update = body.text("div.sub_r > p:eq(3) > span.date");
        String author = body.text("div.sub_r > p:eq(1)");

        // 连载状态
        boolean status = isFinish(body.text("div.sub_r > p:eq(0)"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#mh-chapter-list-ol-0 > li > a")) {
            String title = node.text();
            String path = node.hrefWithSplit(2);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.manhualove.com/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("qTcms_S_m_murl_e=\"(.*?)\"", html, 1);
        if (str != null) {
            try {
                str = DecryptionUtils.base64Decrypt(str);
                String[] array = str.split("\\$qingtiandy\\$");
                String preUrl = "";
                // 当解密出来的URL地址不包含 "https://res.mhkan.com/images/comic" 时，需要加上下面的前缀，否则不需要
                if(!array[0].contains("mhkan")){
                    preUrl = "http://www.dc619.com";
                }
                for (int i = 0; i != array.length; ++i) {
//                    http://www.dc619.com/upload2/20067/2019/03-20/20190320133920_3803el39A.D.cjou_small.jpg
//                    https://res.mhkan.com/images/comic/449/897246/1551916012_ETEbxAU1-ov3pF4.jpg
                    list.add(new ImageUrl(i + 1, preUrl + array[i], false));
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
        return new Node(html).text("div.sub_r > p:eq(3) > span.date");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", "http://m.manhualove.com/");
    }

}
