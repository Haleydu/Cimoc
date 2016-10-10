package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/10/10.
 */

public interface Card {

    Long getId();

    void setId(Long id);

    String getTitle();

    void setTitle(String title);

    int getType();

    void setType(int type);

    boolean getEnable();

    void setEnable(boolean enable);

}
