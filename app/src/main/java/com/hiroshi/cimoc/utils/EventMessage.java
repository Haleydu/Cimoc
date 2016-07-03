package com.hiroshi.cimoc.utils;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class EventMessage {

    public static final int SEARCH_SUCCESS = 1;
    public static final int SEARCH_EMPTY = 2;

    private int type;
    private Object data;

    public EventMessage(int type, Object data) {
        this.type = type;
        this.data = data;
    }

    public int getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

}
