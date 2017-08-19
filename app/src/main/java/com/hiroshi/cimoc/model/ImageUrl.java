package com.hiroshi.cimoc.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Hiroshi on 2016/8/20.
 */
public class ImageUrl {

    private static AtomicInteger count = new AtomicInteger(0);

    public static final int STATE_NULL = 0;
    public static final int STATE_PAGE_1 = 1;
    public static final int STATE_PAGE_2 = 2;

    private int id; // 唯一标识
    private int num;    // 章节的第几页
    private String[] urls;
    private String chapter; // 所属章节
    private int state;  // 切图时表示状态 这里可以改为编号 比如长图可以切为多张方便加载
    private int height; // 图片高度
    private int width;  // 图片宽度
    private boolean lazy;   // 懒加载
    private boolean loading;    // 正在懒加载
    private boolean success;    // 图片显示成功
    private boolean download;   // 下载的图片

    public ImageUrl(int num, String url, boolean lazy) {
        this(num, new String[]{ url }, lazy);
    }

    public ImageUrl(int num, String[] urls, boolean lazy) {
        this(num, urls, STATE_NULL, lazy);
    }

    public ImageUrl(int num, String[] urls, int state, boolean lazy) {
        this(num, urls, null, state, lazy);
    }

    public ImageUrl(int num, String[] urls, String chapter, int state, boolean lazy) {
        this.id = count.getAndAdd(1);
        this.num = num;
        this.urls = urls;
        this.chapter = chapter;
        this.state = state;
        this.height = 0;
        this.width = 0;
        this.lazy = lazy;
        this.loading = false;
        this.success = false;
    }

    public int getId() {
        return id;
    }

    public int getNum() {
        return num;
    }

    public void setUrl(String url) {
        this.urls = new String[]{ url };
    }

    public String[] getUrls() {
        return urls;
    }

    public String getUrl() {
        return urls[0];
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public String getChapter() {
        return chapter;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public long getSize() {
        return height * width;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public boolean isDownload() {
        return download;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImageUrl && ((ImageUrl) o).id == id;
    }

}
