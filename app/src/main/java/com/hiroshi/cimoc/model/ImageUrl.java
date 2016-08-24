package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/8/20.
 */
public class ImageUrl {

    private static int count = 0;

    private int id;
    private int num;
    private String url;
    private boolean lazy;
    private boolean loading;

    public ImageUrl(int num, String url, boolean lazy) {
        this.id = ++count;
        this.num = num;
        this.url = url;
        this.lazy = lazy;
        this.loading = false;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public int getNum() {
        return num;
    }

    public String getUrl() {
        return url;
    }

    public boolean isLazy() {
        return lazy;
    }

    public boolean isLoading() {
        return loading;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImageUrl && ((ImageUrl) o).id == id;
    }

}
