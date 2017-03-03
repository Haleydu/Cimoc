package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/8/20.
 */
public class ImageUrl {

    private static int count = 0;

    private int id;
    private int num;
    private String[] url;
    private boolean lazy;
    private boolean loading;
    private boolean first;

    public ImageUrl(int num, String url, boolean lazy) {
        this(num, new String[]{ url }, lazy);
    }

    public ImageUrl(int num, String[] url, boolean lazy) {
        this(num, url, lazy, true);
    }

    public ImageUrl(int num, String[] url, boolean lazy, boolean first) {
        this.id = ++count;
        this.num = num;
        this.url = url;
        this.lazy = lazy;
        this.first = first;
        this.loading = false;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public void setUrl(String url) {
        this.url = new String[]{ url };
    }

    public int getId() {
        return id;
    }

    public int getNum() {
        return num;
    }

    public String[] getUrl() {
        return url;
    }

    public String getFirstUrl() {
        return url[0];
    }

    public boolean isLazy() {
        return lazy;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isFirst() {
        return first;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImageUrl && ((ImageUrl) o).id == id;
    }

}
