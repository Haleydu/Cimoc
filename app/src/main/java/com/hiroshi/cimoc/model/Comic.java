package com.hiroshi.cimoc.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;

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
    @NotNull private boolean highlight;
    @NotNull private boolean local;
    private String update;
    private Boolean finish;
    private Long favorite;
    private Long history;
    private Long download;
    private String last;
    private Integer page;
    private String chapter;

    @Transient private String intro;
    @Transient private String author;

    public Comic(int source, String cid, String title, String cover, String update, String author) {
        this(null, source, cid, title, cover == null ? "" : cover, false, false, update,
                null, null, null, null, null, null, null);
        this.author = author;
    }

    public Comic(int source, String cid) {
        this.source = source;
        this.cid = cid;
    }

    public Comic(int source, String cid, String title, String cover, long download) {
        this(null, source, cid, title, cover == null ? "" : cover, false, false, null,
                null, null, null, download, null, null, null);
    }

    @Generated(hash = 873921140)
    public Comic(Long id, int source, @NotNull String cid, @NotNull String title, @NotNull String cover, boolean highlight,
            boolean local, String update, Boolean finish, Long favorite, Long history, Long download, String last, Integer page,
            String chapter) {
        this.id = id;
        this.source = source;
        this.cid = cid;
        this.title = title;
        this.cover = cover;
        this.highlight = highlight;
        this.local = local;
        this.update = update;
        this.finish = finish;
        this.favorite = favorite;
        this.history = history;
        this.download = download;
        this.last = last;
        this.page = page;
        this.chapter = chapter;
    }

    @Generated(hash = 1347984162)
    public Comic() {
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Comic && ((Comic) o).id.equals(id);
    }

    public void setInfo(String title, String cover, String update, String intro, String author, boolean finish) {
        if (title != null) {
            this.title = title;
        }
        if (cover != null) {
            this.cover = cover;
        }
        if (update != null) {
            this.update = update;
        }
        this.intro = intro;
        if (author != null) {
            this.author = author;
        }
        this.finish = finish;
        this.highlight = false;
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

    public boolean getHighlight() {
        return this.highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public Long getDownload() {
        return this.download;
    }

    public void setDownload(Long download) {
        this.download = download;
    }

    public Boolean getFinish() {
        return this.finish;
    }

    public void setFinish(Boolean finish) {
        this.finish = finish;
    }

    public boolean getLocal() {
        return this.local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public String getChapter() {
        return this.chapter;
    }

    public void setChapter(String chapter) {
        this.chapter = chapter;
    }

}
