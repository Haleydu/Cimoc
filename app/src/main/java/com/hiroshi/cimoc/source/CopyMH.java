package com.hiroshi.cimoc.source;

import android.util.Log;

import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.JsonIterator;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.HttpUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Request;
import taobe.tec.jcc.JChineseConvertor;

public class CopyMH extends MangaParser {
    public static final int TYPE = 26;
    public static final String DEFAULT_TITLE = "拷贝漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, false);
    }

    public CopyMH(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws Exception {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("https://copymanga.com/search?q=%s", keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public String getUrl(String cid) {
        return "https://copymanga.com/h5/details/comic/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("copymanga.com", "\\w+", 0));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        Node body = new Node(html);
        String kw=body.text("#searchKey");

        JSONObject jsonObject=HttpUtils.httpsRequest("https://www.copymanga.com/api/kb/web/search/count?offset=0&platform=2&limit=50&q="
                .concat(kw),"GET",null);

        JSONArray array=jsonObject.getJSONObject("results").getJSONObject("comic").getJSONArray("list");
        org.json.JSONArray array1= new org.json.JSONArray(array.toString());

        return new JsonIterator(array1) {
            @Override
            protected Comic parse(org.json.JSONObject object) throws JSONException {
                String title = null;
                try {
                    title = object.getString("name");
                    JChineseConvertor jChineseConvertor = JChineseConvertor.getInstance();
                    title = jChineseConvertor.s2t(title);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                String cid = object.getString("path_word");
                String cover = object.getString("cover");

                String author = "";
                org.json.JSONArray jsonObjectList = object.getJSONArray("author");
                for (int i = 0; i < jsonObjectList.length(); ++i) {
                    author += jsonObjectList.getJSONObject(i).getString("name") + " ";
                }
                author = author.trim();

                return new Comic(TYPE, cid, title, cover, null, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://www.copymanga.com/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String cover = body.attr("div.comicParticulars-left-img.loadingIcon > img","data-src");
        String intro = body.text("p.intro");
        String title = body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(1) > h6");

        String update = body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(5) > span.comicParticulars-right-txt");
        String author = body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(3) > span.comicParticulars-right-txt > a");

        // 连载状态
        String status=body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(6) > span.comicParticulars-right-txt");
        boolean finish=isFinish(status);

        comic.setInfo(title, cover, update, intro, author, finish);
    }

    @Override
    public List<Chapter> parseChapter(String html) throws JSONException {
        List<Chapter> list = new LinkedList<>();

        Node body=new Node(html);
        String cid=body.attr("img.lazyload","data-src").replace("https://mirrorvip2.mangafunc.fun/comic/","").replaceAll("/cover.*","");

        JSONObject jsonObject=HttpUtils.httpsRequest(String.format("https://api.copymanga.com/api/v3/comic/%s/group/default/chapters?limit=500&offset=0",cid),"GET",null);
        JSONArray array=jsonObject.getJSONObject("results").getJSONArray("list");

        for (int i=0;i<array.length();++i){
            String title=array.getJSONObject(i).getString("name");
            String path=array.getJSONObject(i).getString("uuid");
            list.add(new Chapter(title,path));
        }

        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://www.copymanga.com/comic/%s/chapter/%s", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) throws Manga.NetworkErrorException, JSONException {
        List<ImageUrl> list = new LinkedList<>();
        Node body=new Node(html);
        String last=body.href("div.footer > div:nth-child(2) > a");
        String next=body.href("div.comicContent-next > a");

        Request request;
        String html1;
        Node body1;
        String current;
        if (last.equals("")) {
            request=new Request.Builder().url("https://www.copymanga.com"+next).build();
            html1= Manga.getResponseBody(App.getHttpClient(), request);
            body1=new Node(html1);
            current=body1.href("div.footer > div:nth-child(2) > a");
        } else {
            request=new Request.Builder().url("https://www.copymanga.com"+last).build();
            html1 = Manga.getResponseBody(App.getHttpClient(), request);
            body1=new Node(html1);
            current=body1.href("div.comicContent-next > a");
        }

        String[] strings=current.split("/");

        JSONObject jsonObject=HttpUtils.httpsRequest(String.format("https://api.copymanga.com/api/v3/comic/%s/chapter/%s?platform=1&_update=true", strings[2],strings[4])
                ,"GET",null);
        JSONArray imgList=jsonObject.getJSONObject("results").getJSONObject("chapter").getJSONArray("contents");
        for (int i=0;i<imgList.length();++i){
            Log.d("hrd",""+imgList.getJSONObject(i).getString("url"));
            list.add(new ImageUrl(i+1,imgList.getJSONObject(i).getString("url"),false));
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {return getInfoRequest(cid);}

    @Override
    public String parseCheck(String html) {
        Node body=new Node(html);
        String update=body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(5) > span.comicParticulars-right-txt");
        return update;
    }
}
