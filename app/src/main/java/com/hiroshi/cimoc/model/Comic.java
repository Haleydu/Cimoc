package com.hiroshi.cimoc.model;

import java.util.List;

/**
 * Created by Hiroshi on 2016/7/3.
 */
public class Comic extends MiniComic {

    private String intro;
    private String status;
    private List<Chapter> list;

    public Comic(String path, String image, String title, int source, String author ,String update, String intro, String status, List<Chapter> list) {
        super(path, image, title, source, author, update);
        this.intro = intro;
        this.status = status;
        this.list = list;
    }

    public List<Chapter> getList() {
        return list;
    }

    public String getIntro() {
        return intro;
    }

}
