package com.hiroshi.cimoc.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Hiroshi on 2016/9/1.
 */
@Entity
public class Task {

    public static final int STATE_FINISH = 0;
    public static final int STATE_PAUSE = 1;
    public static final int STATE_PARSE = 2;
    public static final int STATE_DOING = 3;
    public static final int STATE_WAIT = 4;

    @Id(autoincrement = true) private Long id;
    @NotNull private long key;
    @NotNull private String path;
    @NotNull private String title;
    @NotNull private int progress;
    @NotNull private int max;
    @NotNull private boolean finish;

    @Transient private int source;
    @Transient private String cid;
    @Transient private String comic;
    @Transient private int state;

    @Generated(hash = 727503274)
    public Task(Long id, long key, @NotNull String path, @NotNull String title,
            int progress, int max, boolean finish) {
        this.id = id;
        this.key = key;
        this.path = path;
        this.title = title;
        this.progress = progress;
        this.max = max;
        this.finish = finish;
    }

    @Generated(hash = 733837707)
    public Task() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getKey() {
        return this.key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getProgress() {
        return this.progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean getFinish() {
        return this.finish;
    }

    public void setFinish(boolean finish) {
        this.finish = finish;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getMax() {
        return this.max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getState() {
        return this.state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getSource() {
        return this.source;
    }

    public String getCid() {
        return cid;
    }

    public String getComic() {
        return comic;
    }

    public void setInfo(int source, String cid, String comic) {
        this.source = source;
        this.cid = cid;
        this.comic = comic;
    }

}
