package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class Chapter {

    private String title;
    private String path;
    private int count;

    public Chapter(String title, String path) {
        this.title = title;
        this.path = path;
        this.count = 0;
    }

    public String getTitle() {
        return title;
    }

    public String getPath() {
        return path;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
