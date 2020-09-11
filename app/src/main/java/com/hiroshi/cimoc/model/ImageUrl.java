package com.hiroshi.cimoc.model;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Hiroshi on 2016/8/20.
 */
@Entity
public class ImageUrl {

    public static final int STATE_NULL = 0;
    public static final int STATE_PAGE_1 = 1;
    public static final int STATE_PAGE_2 = 2;

    @Id(autoincrement = true)
    private Long id; // 唯一标识
    @NotNull
    private Long comicChapter;
    private int num;    // 章节的第几页
    @Convert(columnType = String.class, converter = StringConverter.class)
    private String[] urls;
    private String chapter; // 所属章节
    private int state;  // 切图时表示状态 这里可以改为编号 比如长图可以切为多张方便加载
    private int height; // 图片高度
    private int width;  // 图片宽度
    private boolean lazy;   // 懒加载
    private boolean loading;    // 正在懒加载
    private boolean success;    // 图片显示成功
    private boolean download;   // 下载的图片

    public ImageUrl(Long id, Long comicChapter, int num, String[] urls, String chapter, int state, boolean lazy) {
        this(id, comicChapter, num, urls, chapter, state, 0, 0, lazy,
                false, false,false);
    }

    public ImageUrl(Long id,Long comicChapter,int num, String url, boolean lazy) {
       this(id, comicChapter, num, new String[]{url}, null, STATE_NULL,
               0, 0, lazy, false, false, false);
    }

    @Generated(hash = 254698487)
    public ImageUrl(Long id, @NotNull Long comicChapter, int num, String[] urls,
            String chapter, int state, int height, int width, boolean lazy, boolean loading,
            boolean success, boolean download) {
        this.id = id;
        this.comicChapter = comicChapter;
        this.num = num;
        this.urls = urls;
        this.chapter = chapter;
        this.state = state;
        this.height = height;
        this.width = width;
        this.lazy = lazy;
        this.loading = loading;
        this.success = success;
        this.download = download;
    }

    public ImageUrl() {
    }

    public Long getId() {
        return id;
    }

    public int getNum() {
        return num;
    }

    public String[] getUrls() {
        return urls;
    }

    public String getUrl() {
        return urls[0];
    }

    public void setUrl(String url) {
        this.urls = new String[]{url};
    }

    public String getChapter() {
        return chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public long getSize() {
        return height * width;
    }

    public boolean isLazy() {
        return lazy;
    }

    public void setLazy(boolean lazy) {
        this.lazy = lazy;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImageUrl && ((ImageUrl) o).id == id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComicChapter() {
        return this.comicChapter;
    }

    public void setComicChapter(Long comicChapter) {
        this.comicChapter = comicChapter;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    public boolean getLazy() {
        return this.lazy;
    }

    public boolean getLoading() {
        return this.loading;
    }

    public boolean getSuccess() {
        return this.success;
    }

    public boolean getDownload() {
        return this.download;
    }


    public static class StringConverter implements PropertyConverter<String[], String> {
        private static final String SPLIT = "##Cimoc##";

        @Override
        public String[] convertToEntityProperty(String databaseValue) {
            if (databaseValue == null) {
                return null;
            } else {
                return databaseValue.split(SPLIT);
            }
        }

        @Override
        public String convertToDatabaseValue(String[] entityProperty) {
            if (entityProperty == null) {
                return null;
            } else {
                StringBuilder sb = new StringBuilder();
                for (String str : entityProperty) {
                    sb.append(str).append(SPLIT);
                }
                return sb.toString();
            }

        }

    }
}
