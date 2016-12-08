package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.core.parser.NodeIterator;
import com.hiroshi.cimoc.core.parser.SearchIterator;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class IKanman extends MangaParser {

    public IKanman() {
        server = new String[]{ "http://p.yogajx.com", "http://idx0.hamreus.com:8080", "http://ilt2.hamreus.com:8080"};
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("http://m.ikanman.com/s/%s.html?page=%d&ajax=1", keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("li > a")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(1);
                String title = node.text("h3");
                String cover = node.attr("div > img", "data-src");
                String update = node.text("dl:eq(5) > dd");
                String author = node.text("dl:eq(2) > dd");
                return new Comic(SourceManager.SOURCE_IKANMAN, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://m.ikanman.com/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("div.main-bar > h1");
        String cover = body.src("div.book-detail > div.cont-list > div.thumb > img");
        String update = body.text("div.book-detail > div.cont-list > dl:eq(2) > dd");
        String author = body.attr("div.book-detail > div.cont-list > dl:eq(3) > dd > a", "title");
        String intro = body.text("#bookIntro");
        boolean status = isFinish(body.text("div.book-detail > div.cont-list > div.thumb > i"));
        comic.setInfo(title, cover, update, intro, author, status);
    }

    @Override
    public Request getChapterRequest(String html, String cid) {
        String url = "http://m.ikanman.com/support/chapters.aspx?id=".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("div.chapter-list")) {
            List<Chapter> ulList = new LinkedList<>();
            for (Node ul : node.list("ul")) {
                List<Chapter> liList = new LinkedList<>();
                for (Node li : ul.list("li > a")) {
                    String title = li.attr("title");
                    String path = li.hrefWithSplit(2);
                    liList.add(new Chapter(title, path));
                }
                ulList.addAll(0, liList);
            }
            list.addAll(ulList);
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://m.ikanman.com/comic/%s/%s.html", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String str = StringUtils.match("decryptDES\\(\"(.*?)\"\\)", html, 1);
        if (str != null) {
            try {
                String cipherStr = str.substring(8);
                String keyStr = str.substring(0, 8);
                String packed = DecryptionUtils.desDecrypt(keyStr, cipherStr);
                String result = DecryptionUtils.evalDecrypt(packed.substring(4));

                String jsonString = result.substring(11, result.length() - 9);
                JSONArray array = new JSONObject(jsonString).getJSONArray("images");
                for (int i = 0; i != array.length(); ++i) {
                    list.add(new ImageUrl(i + 1, buildUrl(array.getString(i)), false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = StringUtils.format("http://m.ikanman.com/update/?ajax=1&page=%d", page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("li > a")) {
            String cid = node.hrefWithSplit(1);
            String title = node.text("h3");
            String cover = node.attr("div > img", "data-src");
            String update = node.text("dl:eq(5) > dd");
            String author = node.text("dl:eq(2) > dd");
            list.add(new Comic(SourceManager.SOURCE_IKANMAN, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text("div.book-detail > div.cont-list > dl:eq(2) > dd");
    }

}
