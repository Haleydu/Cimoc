package com.hiroshi.cimoc.model;

import com.hiroshi.cimoc.core.Administrator;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MiniComic {

    private String url;
    private String image;
    private String title;
    private String author;
    private String update;
    private int source;

    public MiniComic(String url, String image, String title, int source, String author, String update) {
        this.url = url;
        this.image = image;
        this.title = title;
        this.source = source;
        this.author = author;
        this.update = update;
    }

    public String getUrl() { return Administrator.getHostById(source) + url; }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() { return author; }

    public String getUpdate() { return update; }

    public int getSource() {
        return source;
    }

}

