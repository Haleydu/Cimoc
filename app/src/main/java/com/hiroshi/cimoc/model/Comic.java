package com.hiroshi.cimoc.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Hiroshi on 2016/7/20.
 */
@Entity
public class Comic {

    @Id(autoincrement = true) private Long id;
    @NotNull private int source;
    @NotNull private String cid;
    @NotNull private String title;
    @NotNull private String cover;
    @NotNull private String update;
    private Long favorite;
    private Long history;
    private String last;
    private Integer page;

    @Transient private String intro;
    @Transient private String author;
    @Transient private Boolean status;

    @Generated(hash = 1562578524)
    public Comic(Long id, int source, @NotNull String cid, @NotNull String title, @NotNull String cover,
                 @NotNull String update, Long favorite, Long history, String last, Integer page) {
        this.id = id;
        this.source = source;
        this.cid = cid;
        this.title = title;
        this.cover = cover;
        this.update = update;
        this.favorite = favorite;
        this.history = history;
        this.last = last;
        this.page = page;
    }

    @Generated(hash = 1347984162)
    public Comic() {
    }

    public Comic(int source, String cid, String title, String cover, String update, String author, Boolean status) {
        this.source = source;
        this.cid = cid;
        this.title = title;
        this.cover = cover;
        this.update = update;
        this.author = author;
        this.status = status;
    }

    public Comic(int source, String cid) {
        this.source = source;
        this.cid = cid;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Comic && ((Comic) o).id.equals(id);
    }

    public void setInfo(String title, String cover, String update, String intro, String author, boolean status) {
        this.title = title;
        this.cover = cover;
        this.update = update;
        this.intro = intro;
        this.author = author;
        this.status = status;
    }

    public String getIntro() {
        return this.intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Boolean getStatus() {
        return this.status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getPage() {
        return this.page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getLast() {
        return this.last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public Long getHistory() {
        return this.history;
    }

    public void setHistory(Long history) {
        this.history = history;
    }

    public Long getFavorite() {
        return this.favorite;
    }

    public void setFavorite(Long favorite) {
        this.favorite = favorite;
    }

    public String getUpdate() {
        return this.update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public String getCover() {
        return this.cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCid() {
        return this.cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public int getSource() {
        return this.source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
