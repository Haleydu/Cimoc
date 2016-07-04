package com.hiroshi.cimoc.model;

import com.hiroshi.cimoc.core.Kami;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class MiniComic {

    private String path;
    private String image;
    private String title;
    private String author;
    private String update;
    private int source;

    public MiniComic(String path, String image, String title, int source, String author, String update) {
        this.path = path;
        this.image = image;
        this.title = title;
        this.source = source;
        this.author = author;
        this.update = update;
    }

    public String getPath() { return path; }

    public String getUrl() { return Kami.getHostById(source) + path; }

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

