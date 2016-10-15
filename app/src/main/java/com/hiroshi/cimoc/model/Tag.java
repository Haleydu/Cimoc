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

    @Generated(hash = 836804519)
    public Tag(Long id, @NotNull String title) {
        this.id = id;
        this.title = title;
    }

    @Generated(hash = 1605720318)
    public Tag() {
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Tag && ((Tag) o).id.equals(id);
    }

    @Override
    public int hashCode() {
        return id == null ? super.hashCode() : id.hashCode();
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

}
