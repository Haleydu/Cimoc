package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class Comic {

    private int img;
    private String title;
    private String res;

    public Comic(int img, String title, String res) {
        this.img = img;
        this.title = title;
        this.res = res;
    }

    public int getImg() {
        return img;
    }

    public String getTitle() {
        return title;
    }

    public String getRes() {
        return res;
    }

}
