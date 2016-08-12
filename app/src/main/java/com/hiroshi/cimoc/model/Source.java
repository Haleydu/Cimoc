package com.hiroshi.cimoc.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Hiroshi on 2016/8/11.
 */
@Entity
public class Source {

    @Id private Long id;
    @Unique private int sid;
    @NotNull private boolean enable;

    public boolean getEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getSid() {
        return this.sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Generated(hash = 667633842)
    public Source(Long id, int sid, boolean enable) {
        this.id = id;
        this.sid = sid;
        this.enable = enable;
    }

    @Generated(hash = 615387317)
    public Source() {
    }

}
