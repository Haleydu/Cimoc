package com.hiroshi.cimoc.source;

import android.net.Uri;
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

import java.util.ArrayList;
import java.util.Arrays;
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

    String decryptKey1Arr[] = {"fw122587mkertyui", "fw12558899ertyui"};
    String decryptKey2Arr[] = {"fw125gjdi9ertyui"};
    @Override
    public List<ImageUrl> parseImages(String html, Chapter chapter) {
        String encodedData = StringUtils.match("C_DATA=\\'(.+?)\\'", html, 1);
        if (encodedData != null) {
            try {

                String decryptedData = decodeAndDecrypt("encodedData", encodedData, decryptKey1Arr);
                String imgType = StringUtils.match( "img_type:\"(.+?)\"",decryptedData,1);

                 if (imgType != null && imgType.isEmpty()) {
                     return processPagesFromExternal(decryptedData, chapter);
                } else {
                     return  processPagesFromInternal(decryptedData, chapter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String decodeAndDecrypt(String decodeName, String value, String[] keyArr)  {
        String decodedValue = new String(Base64.decode(value, Base64.NO_WRAP));
        for (int i = 0; i < keyArr.length; i++) {
            try {
                return DecryptionUtils.decryptAES(decodedValue, keyArr[i]);
            } catch (Exception e) {

            }
        }
        return "";
    }


    private List<ImageUrl> processPagesFromExternal(String decryptedData, Chapter chapter){
        List<ImageUrl> list = new LinkedList<>();
        String encodedUrlsDirect =  StringUtils.match( "urls__direct:\"(.+?)\"", decryptedData,1);
        String decodedUrlsDirect = new  String(Base64.decode(encodedUrlsDirect, Base64.NO_WRAP));
        String pageUrlArr[] = decodedUrlsDirect.split("|SEPARATER|");

        for (int i = 1; i <= pageUrlArr.length; ++i) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            list.add(new ImageUrl(id, comicChapter, i, pageUrlArr[i-1], false));
        }
        return list;
    }

    private List<ImageUrl> processPagesFromInternal(String decryptedData, Chapter chapter){
        List<ImageUrl> list = new LinkedList<>();
        String imageServerDomain = StringUtils.match(  "domain:\"(.+?)\"", decryptedData,1);
        int startImg = Integer.parseInt(StringUtils.match(  "startimg:([0-9]+?),", decryptedData,1));
        String encodedRelativePath = StringUtils.match(  "enc_code2:\"(.+?)\"", decryptedData,1);
        String decryptedRelativePath = decodeAndDecrypt("encodedRelativePath", encodedRelativePath, decryptKey2Arr);
        String encodedTotalPages = StringUtils.match("enc_code1:\"(.+?)\"", decryptedData,1);
        int decryptedTotalPages = Integer.parseInt(decodeAndDecrypt("encodedTotalPages", encodedTotalPages, decryptKey1Arr));
        for (int i = startImg; i <= decryptedTotalPages; ++i) {
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            String jpg = StringUtils.format("%04d.jpg", i);
            String url = "https://" + imageServerDomain+"/comic/"+encodeUri(decryptedRelativePath)+jpg;
            list.add(new ImageUrl(id, comicChapter, i, url, false));
        }
        return list;
    }

    private String encodeUri(String str){
        String whitelistChar = "@#&=*+-_.,:!?()/~'%";
        return Uri.encode(str, whitelistChar);
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

