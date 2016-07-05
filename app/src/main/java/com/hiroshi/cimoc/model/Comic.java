package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/7/1.
 */
public class Comic {

    private int source;
    private String path;
    private String image;
    private String title;
    private String intro;
    private String author;
    private String status;
    private String update;

    public Comic(int source, String path, String image, String title, String author, String intro, String status, String update) {
        this.path = path;
        this.image = image;
        this.title = title;
        this.source = source;
        this.author = author;
        this.update = update;
        this.status = status;
        this.intro = intro;
    }

    public String getPath() { return path; }

    public String getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() { return author; }

    public String getUpdate() { return update; }

    public String getStatus() {
        return status;
    }

    public String getIntro() {
        return intro;
    }

    public int getSource() {
        return source;
    }

}

