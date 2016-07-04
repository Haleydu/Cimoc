package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.BaseSearch;
import com.hiroshi.cimoc.model.MiniComic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class IKanmanSearch extends BaseSearch {

    @Override
    protected List<MiniComic> parseResult(String html) {
        Document doc = Jsoup.parse(html);
        Elements items = doc.select("#detail > li > a");
        List<MiniComic> list = new LinkedList<>();
        for (Element item : items) {
            String url = item.attr("href");
            String image = item.select("div > img").first().attr("data-src");
            String title = item.select("h3").first().text();
            String author = item.select("dl > dd").get(0).text();
            String update = item.select("dl > dd").get(3).text();
            list.add(new MiniComic(url, image, title, Kami.SOURCE_IKANMAN, author, update));
        }
        return list;
    }

    @Override
    protected String getUrl(String keyword, int page) {
        return String.format(Locale.SIMPLIFIED_CHINESE, "http://m.ikanman.com/s/%s.html?page=%d", keyword, page);
    }

}
