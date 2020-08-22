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

import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by ZhiWen on 2019/02/25.
 * fix by haleydu on2020/8/20
 */

public class MH160 extends MangaParser {

    public static final int TYPE = 28;
    public static final String DEFAULT_TITLE = "漫画160";
    private static final String baseUrl = "https://m.mh160.co";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    public MH160(Source source) {
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
                .addHeader("Host","www.mh160.co")
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
    public String getUrl(String cid) {
        return baseUrl + cid;
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseUrl + cid;
        return new Request.Builder()
                .url(url)
                .addHeader("Referer", baseUrl)
                .addHeader("Host","www.mh160.co")
                .build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String cover = body.src(".mh-date-bgpic > a > img");
        String intro = body.text("#workint > p");
        String title = body.attr(".mh-date-bgpic > a > img", "title");
        String update = body.text("div.cy_zhangjie_top > :eq(2) > font");
        String author = body.text("span.one > em");
        boolean status = isFinish(body.text("p.works-info-tc > span:eq(3)"));

        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("#mh-chapter-list-ol-0 > li > a")) {
            String title = node.text("p");
            String path = node.href();
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = baseUrl + path;
        return new Request.Builder()
                .url(url)
                .addHeader("Referer", baseUrl)
                .addHeader("Host","www.mh160.co")
                .build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("qTcms_S_m_murl_e=\"(.*?)\"", html, 1);
        String id = StringUtils.match("qTcms_S_p_id=\"(.*?)\"", html, 1);
        if (str != null) {
            try {
                str = DecryptionUtils.base64Decrypt(str);
                String[] array = str.split("\\$qingtiandy\\$");
                String preUrl = "";
                if(Integer.parseInt(id)>542724){
                    preUrl = " https://mhpic5.miyeye.cn:20208";
                }else {
                    preUrl = "https://res.gezhengzhongyi.cn:20207";
                }
                if (Integer.parseInt(id)>884998){
                    preUrl = "https://mhpic88.miyeye.cn:20207";
                }

                for (int i = 0; i != array.length; ++i) {
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
        return new Node(html).text("div.cy_zhangjie_top > :eq(2) > font");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", baseUrl);
    }

}
