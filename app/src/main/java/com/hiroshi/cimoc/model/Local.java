package com.hiroshi.cimoc.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Hiroshi on 2017/5/14.
 */
@Entity
public class Local {

    @Id(autoincrement = true) private Long id;
    @NotNull private String title;
    @NotNull private String path;
    private String last;
    private Integer page;

    @Generated(hash = 649783019)
    public Local(Long id, @NotNull String title, @NotNull String path, String last,
            Integer page) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.last = last;
        this.page = page;
    }

    @Generated(hash = 1337064102)
    public Local() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getLast() {
        return this.last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public Integer getPage() {
        return this.page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

}
