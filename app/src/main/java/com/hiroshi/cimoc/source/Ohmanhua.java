package com.hiroshi.cimoc.source;

import android.util.Base64;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Haleyd on 2020/8/7.
 */

public class Ohmanhua extends MangaParser {

    public static final int TYPE = 71;
    public static final String DEFAULT_TITLE = "oh漫画";
    private static final String baseUrl = "https://www.cocomanhua.com";
    private static final String serverUrl = "https://img.cocomanhua.com/comic/";

    public Ohmanhua(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = "";
        if (page == 1) {
            url = StringUtils.format(baseUrl+"/search?searchString=%s", keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("dl.fed-deta-info")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.href("dd > h1 > a");
                String title = node.text("dd > h1 > a");
                String cover = node.attr("dt > a", "data-original");
                String author = node.text("dd > ul > li:eq(3)");
                String update = node.text("dd > ul > li:eq(4)");

                if (!author.contains("作者")){
                    author = update;
                }
                if (!update.contains("更新")){
                    update = node.text("dd > ul > li:eq(5)");
                }
                return new Comic(TYPE, cid, title, cover,
                        update.replace("更新",""),
                        author.replace("作者",""));
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return baseUrl+cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.cocomanhua.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseUrl + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("dl.fed-deta-info > dd > h1");
        String cover = body.attr("dl.fed-deta-info > dt > a","data-original");
        String intro = body.text("div.fed-tabs-boxs > div > p");
        String statusStr = body.text("dl.fed-deta-info > dd > ul > li:eq(0)");
        String author = body.text("dl.fed-deta-info > dd > ul > li:eq(1)");
        String update = body.text("dl.fed-deta-info > dd > ul > li:eq(2)");

        if (!statusStr.contains("状态")){
            statusStr = author;
        }
        if (!author.contains("作者")){
            author = update;
        }
        if (!update.contains("更新")){
            update = body.text("dl.fed-deta-info > dd > ul > li:eq(3) > a");
        }
        boolean status = isFinish(statusStr.replace("状态",""));

        comic.setInfo(title, cover, update.replace("更新",""), intro, author.replace("作者",""), status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        int i=0;
        for (Node node : new Node(html).list("div:not(.fed-hidden) > div.all_data_list > ul.fed-part-rows a")) {
            String title = node.attr("title");
            String path = node.href("a");

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
        String encodedData = StringUtils.match("C_DATA=\\'(.+?)\\'", html, 1);
        if (encodedData != null) {
            try {
                String decryptKey = "fw12558899ertyui";
                String decodedData  = new String(Base64.decode(encodedData, Base64.DEFAULT));
                String decryptedData = DecryptionUtils.decryptAES(decodedData, decryptKey);
                String imgRelativePath = StringUtils.match("enc_code2:\"(.+?)\"",decryptedData,1);
                imgRelativePath  = new String(Base64.decode(imgRelativePath, Base64.DEFAULT));
                imgRelativePath = DecryptionUtils.decryptAES(imgRelativePath, "fw125gjdi9ertyui");
                String startImg = StringUtils.match("startimg:([0-9]+?),",decryptedData,1);
                String totalPages = StringUtils.match("enc_code1:\"(.+?)\",",decryptedData,1);
                totalPages  = new String(Base64.decode(totalPages, Base64.DEFAULT));
                totalPages = DecryptionUtils.decryptAES(totalPages, decryptKey);

                for (int i = Integer.parseInt(Objects.requireNonNull(startImg));
                     i <= Integer.parseInt(Objects.requireNonNull(totalPages)); ++i) {
                    Long comicChapter = chapter.getId();
                    Long id = Long.parseLong(comicChapter + "000" + i);
                    String jpg = StringUtils.format("%04d.jpg", i);
                    list.add(new ImageUrl(id, comicChapter, i, serverUrl + imgRelativePath + jpg, false));
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
        Node body = new Node(html);
        String update = body.text("dl.fed-deta-info > dd > ul > li:eq(2)");
        if (!update.contains("更新")){
            update = body.text("dl.fed-deta-info > dd > ul > li:eq(3) > a");
        }
        return update.replace("更新","");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", baseUrl);
    }


}

