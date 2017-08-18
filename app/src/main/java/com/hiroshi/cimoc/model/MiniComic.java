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
    private Boolean finish;
    private boolean highlight;
    private boolean local;

    public MiniComic(Comic comic) {
        this.id = comic.getId();
        this.source = comic.getSource();
        this.cid = comic.getCid();
        this.title = comic.getTitle();
        this.cover = comic.getCover();
        this.finish = comic.getFinish();
        this.highlight = comic.getHighlight();
        this.local = comic.getLocal();
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
        return finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public boolean isHighlight() {
        return highlight;
    }

    public boolean isLocal() {
        return local;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
