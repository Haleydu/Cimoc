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
public class DownloadTask {

    @Id(autoincrement = true) private Long id;
    @NotNull private long cid;
    @NotNull private int progress;
    @NotNull private boolean finish;

    @Transient private int source;
    @Transient private boolean download;

    @Generated(hash = 456746223)
    public DownloadTask(Long id, long cid, int progress, boolean finish) {
        this.id = id;
        this.cid = cid;
        this.progress = progress;
        this.finish = finish;
    }

    @Generated(hash = 1999398913)
    public DownloadTask() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getCid() {
        return this.cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
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

    public boolean getDownload() {
        return this.download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    public int getSource() {
        return this.source;
    }

    public void setSource(int source) {
        this.source = source;
    }

}
