package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.utils.Decryption;
import com.hiroshi.cimoc.utils.MachiSoup;
import com.hiroshi.cimoc.utils.MachiSoup.Node;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/7/8.
 */
public class IKanman extends Manga {

    public IKanman() {
        super(Kami.SOURCE_IKANMAN, "http://m.ikanman.com");
    }

    @Override
    protected Request buildSearchRequest(String keyword, int page) {
        String url = host + "/s/" + keyword + ".html?page=" + page;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Comic> parseSearch(String html) {
        Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (Node node : body.list("#detail > li > a")) {
            String cid = node.attr("href", "/", 2);
            String title = node.text("h3");
            String cover = node.attr("div > img", "data-src");
            String update = node.text("dl:eq(5) > dd");
            String author = node.text("dl:eq(2) > dd");
            boolean status = "完结".equals(node.text("div > i"));
            list.add(new Comic(source, cid, title, cover, update, author, status));
        }
        return list;
    }

    @Override
    protected Request buildIntoRequest(String cid) {
        String url = host + "/comic/" + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected List<Chapter> parseInto(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        Node body = MachiSoup.body(html);
        for (Node node : body.list("#chapterList > ul > li > a")) {
            String c_title = node.text("b");
            String c_path = node.attr("href", "/|\\.", 3);
            list.add(new Chapter(c_title, c_path));
        }

        String title = body.text("div.main-bar > h1");
        Node detail = body.select("div.book-detail");
        String cover = detail.attr("div:eq(0) > div:eq(0) > img", "src");
        String update = detail.text("div:eq(0) > dl:eq(2) > dd");
        String author = detail.attr("div:eq(0) > dl:eq(3) > dd > a", "title");
        Node temp = detail.id("bookIntro");
        String intro = temp.exist("p:eq(0)") ? temp.text() : temp.text("p:eq(0)");
        boolean status = "完结".equals(detail.text("div:eq(0) > div:eq(0) > i"));
        comic.setInfo(title, cover, update, intro, author, status);

        return list;
    }

    @Override
    protected Request buildBrowseRequest(String cid, String path) {
        String url = host + "/comic/" + cid + "/" + path + ".html";
        return new Request.Builder().url(url).build();
    }

    @Override
    protected String[] parseBrowse(String html) {
        String str = MachiSoup.match("decryptDES\\(\"(.*?)\"\\)", html, 1);
        if (str != null) {
            try {
                String cipherStr = str.substring(8);
                String keyStr = str.substring(0, 8);
                String packed = Decryption.desDecrypt(keyStr, cipherStr);
                String result = Decryption.evalDecrypt(packed.substring(4));

                String jsonString = result.substring(11, result.length() - 9);
                JSONObject info = new JSONObject(jsonString);
                JSONArray array = info.getJSONArray("images");
                String[] images = new String[array.length()];
                for (int i = 0; i != array.length(); ++i) {
                    images[i] = "http://i.hamreus.com:8080" + array.getString(i);
                }
                return images;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected Request buildCheckRequest(String cid) {
        String url = host + "/comic/" + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    protected String parseCheck(String html) {
        Node body = MachiSoup.body(html);
        return body.text("div.book-detail > div:eq(0) > dl:eq(2) > dd");
    }

}
