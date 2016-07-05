package com.hiroshi.cimoc.core;

import com.hiroshi.cimoc.core.base.BaseSearch;
import com.hiroshi.cimoc.model.Comic;

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
    protected List<Comic> parse(String html) {
        Document doc = Jsoup.parse(html);
        Elements items = doc.select("#detail > li > a");
        List<Comic> list = new LinkedList<>();
        for (Element item : items) {
            String path = item.attr("href");
            String image = item.select("div > img").first().attr("data-src");
            String status = item.select("div > i").first().text();
            String title = item.select("h3").first().text();
            String author = item.select("dl:eq(2) > dd").first().text();
            String update = item.select("dl:eq(5) > dd").first().text();
            list.add(new Comic(Kami.SOURCE_IKANMAN, path, image, title, author, null, status, update));
        }
        return list;
    }

    @Override
    protected String getUrl(String keyword, int page) {
        return String.format(Locale.SIMPLIFIED_CHINESE, "http://m.ikanman.com/s/%s.html?page=%d", keyword, page);
    }

}
