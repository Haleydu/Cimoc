package com.hiroshi.cimoc.core;

import android.util.Log;

import com.hiroshi.cimoc.core.base.Manga;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.utils.EventMessage;
import com.hiroshi.cimoc.utils.YuriClient;

import org.greenrobot.eventbus.EventBus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class IKanman extends Manga {

    private static IKanman instance;

    private IKanman() {

    }

    public void search(String keyword, int page){
        String url = String.format(Locale.SIMPLIFIED_CHINESE, "http://m.ikanman.com/s/%s.html?page=%d", keyword, page);
        client.enqueue(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String result = response.body().string();
                    List<MiniComic> list = parseSearch(result);
                    if (list.isEmpty()) {
                        EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_EMPTY, null));
                    } else {
                        EventBus.getDefault().post(new EventMessage(EventMessage.SEARCH_SUCCESS, list));
                    }
                }
            }
        });
    }

    private List<MiniComic> parseSearch(String html) {
        Document doc = Jsoup.parse(html);
        Elements items = doc.select("#detail > li > a");
        List<MiniComic> list = new LinkedList<>();
        for (Element item : items) {
            String url = item.attr("href");
            String image = item.select("div > img").first().attr("data-src");
            String title = item.select("h3").first().text();
            String author = item.select("dl > dd").get(0).text();
            String update = item.select("dl > dd").get(3).text();
            list.add(new MiniComic(url, image, title, Administrator.SOURCE_IKANMAN, author, update));
        }
        return list;
    }

    private MiniComic parseComic(String html) {
        Document doc = Jsoup.parse(html);
        String title = doc.select("div.main-bar > h1").first().text();
        Element detail = doc.getElementsByClass("cont-list").first();
        String img = detail.select("div.thumb > img").first().attr("src");
        String status = detail.select("div.thumb > i").first().text();
        String update = detail.select("dl:eq(2) > dd").first().text();
        String author = detail.select("dl:eq(3) > dd > a").first().attr("title");
        return null;
    }

    public static IKanman getInstance() {
        if (instance == null) {
            instance = new IKanman();
        }
        return instance;
    }
}
