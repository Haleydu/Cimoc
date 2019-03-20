package com.hiroshi.cimoc.soup;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by reborn on 18-1-21.
 */

public class MDocument {

    private Document document;

    public MDocument(String html) {
        this.document = Jsoup.parse(html);
    }

    public String text(String cssQuery) {
        try {
//            Elements elements = document.getElementsByTag("script").eq(7);
            Elements elements = document.select(cssQuery);
            return String.valueOf(elements.first());
        } catch (Exception e) {
            return null;
        }
    }
}
