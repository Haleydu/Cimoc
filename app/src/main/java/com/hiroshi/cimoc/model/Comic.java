package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class Comic extends MiniComic {

    private String intro;
    private String status;

    public Comic(String url, String image, String title, int source, String author ,String update, String intro, String status) {
        super(url, image, title, source, author, update);
        this.intro = intro;
        this.status = status;
    }

}
