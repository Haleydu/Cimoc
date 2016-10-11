package com.hiroshi.cimoc.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by Hiroshi on 2016/10/10.
 */
@Entity
public class TagRef {

    @Id(autoincrement = true) private Long id;
    @NotNull private long tid;
    @NotNull private long cid;

    @Generated(hash = 1744842042)
    public TagRef(Long id, long tid, long cid) {
        this.id = id;
        this.tid = tid;
        this.cid = cid;
    }

    @Generated(hash = 942776696)
    public TagRef() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getTid() {
        return this.tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public long getCid() {
        return this.cid;
    }

    public void setCid(long cid) {
        this.cid = cid;
    }

}
