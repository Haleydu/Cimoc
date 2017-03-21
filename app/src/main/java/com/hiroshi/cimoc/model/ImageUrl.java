package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/8/20.
 */
public class ImageUrl {

    private static int count = 0;

    public static final int STATE_NULL = 0;
    public static final int STATE_PAGE_1 = 1;
    public static final int STATE_PAGE_2 = 2;
    public static final int STATE_DONE = 3;

    private int id;
    private int num;
    private String[] url;
    private boolean lazy;
    private boolean loading;
    private int state;

    public ImageUrl(int num, String url, boolean lazy) {
        this(num, new String[]{ url }, lazy);
    }

    public ImageUrl(int num, String[] url, boolean lazy) {
        this(num, url, lazy, STATE_NULL);
    }

    public ImageUrl(int num, String[] url, boolean lazy, int state) {
        this.id = ++count;
        this.num = num;
        this.url = url;
        this.lazy = lazy;
        this.state = state;
        this.loading = false;
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

    public void setState(int state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public int getNum() {
        return num;
    }

    public String[] getUrls() {
        return url;
    }

    public String getUrl() {
        return url[0];
    }

    public int getState() {
        return state;
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
