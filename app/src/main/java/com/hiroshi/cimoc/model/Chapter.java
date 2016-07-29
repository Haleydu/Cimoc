package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class Chapter {

    String title;
    String path;

    public Chapter(String title, String path) {
        this.title = title;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

}
