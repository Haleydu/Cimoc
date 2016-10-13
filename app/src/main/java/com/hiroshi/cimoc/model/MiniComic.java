package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/7/27.
 */
public class MiniComic {

    private Long id;
    private int source;
    private String cid;
    private String title;
    private String cover;
    private String update;
    private Boolean finish;
    private boolean highlight;

    public MiniComic(Comic comic) {
        this.id = comic.getId();
        this.source = comic.getSource();
        this.cid = comic.getCid();
        this.title = comic.getTitle();
        this.cover = comic.getCover();
        this.update = comic.getUpdate();
        this.finish = comic.getFinish();
        this.highlight = comic.getHighlight();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MiniComic && ((MiniComic) o).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : id.hashCode();
    }

    public Boolean isFinish() {
        return this.finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public boolean isHighlight() {
        return this.highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
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
