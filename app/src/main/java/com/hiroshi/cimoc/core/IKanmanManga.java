package com.hiroshi.cimoc.core;

import android.util.Log;

import com.hiroshi.cimoc.core.base.BaseManga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/4.
 */
public class IKanmanManga extends BaseManga {

    @Override
    protected Comic parse(String html, List<Chapter> list) {
        Document doc = Jsoup.parse(html);
        Elements items = doc.select("#chapterList > ul > li > a");
        for (Element item : items) {
            String c_title = item.select("b").first().text();
            String c_path = item.attr("href");
            list.add(new Chapter(c_title, c_path));
        }
        String title = doc.select(".main-bar > h1").first().text();
        Element detail = doc.getElementsByClass("book-detail").first();
        Element cont = detail.getElementsByClass("cont-list").first();
        String image = cont.select(".thumb > img").first().attr("src");
        String status = cont.select(".thumb > i").first().text();
        String update = cont.select("dl:eq(2) > dd").first().text();
        String author = cont.select("dl:eq(3) > dd > a").first().attr("title");
        Element node = detail.getElementById("bookIntro");
        String intro;
        if (node.select("p:eq(0)").isEmpty()) {
            intro = node.text();
        } else {
            intro = node.select("p:eq(0)").first().text();
        }
        return new Comic(0, null, image, title, author, intro, status, update);
    }

}
