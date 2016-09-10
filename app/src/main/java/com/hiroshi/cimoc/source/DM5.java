package com.hiroshi.cimoc.source;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.core.parser.MangaParser;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.soup.MachiSoup;
import com.hiroshi.cimoc.utils.DecryptionUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/25.
 */
public class DM5 extends MangaParser {

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = String.format(Locale.getDefault(), "http://www.dm5.com/search?page=%d&title=%s", page, keyword);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseSearch(String html, int page) {
        MachiSoup.Node body = MachiSoup.body(html);
        List<Comic> list = new LinkedList<>();
        for (MachiSoup.Node node : body.list("div.midBar > div.item")) {
            String cid = node.attr("dt > p > a.title", "href", "/", 1);
            String title = node.text("dt > p > a.title");
            String cover = node.attr("dl > a > img", "src");
            String update = node.text("dt > p > span.date", 6, -7);
            String author = node.text("dt > a:eq(2)");
            // boolean status = "已完结".equals(node.text("dt > p > span.date > span.red", 1, -2));
            list.add(new Comic(SourceManager.SOURCE_DM5, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://www.dm5.com/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Chapter> parseInfo(String html, Comic comic) {
        List<Chapter> list = new LinkedList<>();
        MachiSoup.Node body = MachiSoup.body(html);
        int count = 0;
        for (MachiSoup.Node node : body.list("ul[id^=cbc_] > li > a")) {
            String c_title = node.text();
            try {
                String c_path = node.attr("href", "/", 1);
                if (count % 4 == 0) {
                    String[] array = c_title.split(" ", 2);
                    if (array.length == 2) {
                        c_title = array[1];
                    }
                }
                list.add(new Chapter(c_title, c_path));
            } catch (Exception e) {
                e.printStackTrace();
            }
            ++count;
        }

        String title = body.text("#mhinfo > div.inbt > h1.new_h2");
        String cover = body.attr("#mhinfo > div.innr9 > div.innr90 > div.innr91 > img", "src");
        String update = body.text("#mhinfo > div.innr9 > div.innr90 > div.innr92 > span:eq(9)", 5, -10);
        String author = body.text("#mhinfo > div.innr9 > div.innr90 > div.innr92 > span:eq(2) > a");
        String intro = body.text("#mhinfo > div.innr9 > div.mhjj > p").replace("[+展开]", "").replace("[-折叠]", "");
        boolean status = "已完结".equals(body.text("#mhinfo > div.innr9 > div.innr90 > div.innr92 > span:eq(6)", 5));
        comic.setInfo(title, cover, update, intro, author, status);

        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = "http://www.dm5.com/".concat(path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String[] rs = MachiSoup.match("var DM5_CID=(.*?);\\s*var DM5_IMAGE_COUNT=(\\d+);", html, 1, 2);
        if (rs != null) {
            String format = "http://www.dm5.com/m%s/chapterfun.ashx?cid=%s&page=%d";
            String packed = MachiSoup.match("eval(.*?)\\s*</script>", html, 1);
            if (packed != null) {
                String key = MachiSoup.match("comic=(.*?);", DecryptionUtils.evalDecrypt(packed), 1);
                if (key != null) {
                    key = key.replaceAll("'|\\+", "");
                    format = format.concat("&key=").concat(key);
                }
            }
            int page = Integer.parseInt(rs[1]);
            for (int i = 0; i != page; ++i) {
                list.add(new ImageUrl(i + 1, String.format(Locale.getDefault(), format, rs[0], rs[0], i + 1), true));
            }
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder().url(url).header("Referer", "http://www.dm5.com").build();
    }

    @Override
    public String parseLazy(String html, String url) {
        String result = DecryptionUtils.evalDecrypt(html);
        if (result != null) {
            return result.split(",")[0];
        }
        return null;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return MachiSoup.body(html).text("#mhinfo > div.innr9 > div.innr90 > div.innr92 > span:eq(9)", 5, -10);
    }

}
