package com.hiroshi.cimoc.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

/**
 * Created by Hiroshi on 2016/10/10.
 */
@Entity
public class Tag {

    @Id(autoincrement = true) private Long id;
    @NotNull private String title;
    @NotNull private int count;
    private String cover;

    @Generated(hash = 1976843784)
    public Tag(Long id, @NotNull String title, int count, String cover) {
        this.id = id;
        this.title = title;
        this.count = count;
        this.cover = cover;
    }

    @Generated(hash = 1605720318)
    public Tag() {
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

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getCover() {
        return this.cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

}
