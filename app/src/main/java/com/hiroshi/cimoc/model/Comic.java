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
    @NotNull private Integer source;
    @NotNull private String path;
    @NotNull private String title;
    @NotNull private String image;
    @NotNull private String update;
    private Long favorite;
    private Long history;
    private String last;
    private Integer page;

    @Transient private String intro;
    @Transient private String author;
    @Transient private Boolean status;

    @Generated(hash = 1595116821)
    public Comic(Long id, @NotNull Integer source, @NotNull String path,
            @NotNull String title, @NotNull String image, @NotNull String update,
            Long favorite, Long history, String last, Integer page) {
        this.id = id;
        this.source = source;
        this.path = path;
        this.title = title;
        this.image = image;
        this.update = update;
        this.favorite = favorite;
        this.history = history;
        this.last = last;
        this.page = page;
    }

    @Generated(hash = 1347984162)
    public Comic() {
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Comic && ((Comic) o).id.equals(id);
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

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getSource() {
        return this.source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
