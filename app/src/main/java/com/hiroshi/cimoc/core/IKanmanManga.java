package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.BaseManga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.utils.EventMessage;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class IKanmanManga extends BaseManga {

    public void parse(final String path, final int source) {
        String url = Kami.getHostById(source) + path;
        client.enqueue(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String html = response.body().string();
                    Comic comic = parseComic(html, path, source);
                    EventBus.getDefault().post(new EventMessage(EventMessage.LOAD_COMIC_SUCCESS, comic));
                }
            }
        });
    }

    private Comic parseComic(String html, String path, int source) {
        Document doc = Jsoup.parse(html);
        String title = doc.select(".main-bar > h1").first().text();
        Element detail = doc.getElementsByClass("book-detail").first();
        String image = detail.select(".cont-list > .thumb > img").first().attr("src");
        String status = detail.select(".cont-list > .thumb > i").first().text();
        String update = detail.select(".cont-list > dl > dd").get(1).text();
        String author = detail.select(".cont-list > dl > dd").get(2).select("a").first().attr("title");
        String intro = detail.getElementById("bookIntro").select("p").first().text();
        Elements items = doc.select("#chapterList > ul > li > a");
        List<Chapter> list = new LinkedList<>();
        for (Element item : items) {
            String c_title = item.select("b").first().text();
            String c_path = item.attr("href");
            list.add(new Chapter(c_title, c_path));
        }
        return new Comic(path, image, title, source, author, update, intro, status, list);
    }

}
