package com.hiroshi.cimoc.utils;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class EventMessage {

    public static final int SEARCH_SUCCESS = 1;
    public static final int LOAD_COMIC_SUCCESS = 2;
    public static final int PARSE_PIC_SUCCESS = 3;
    public static final int PARSE_PIC_FAIL = 4;
    public static final int NETWORK_ERROR = 5;
    public static final int FAVORITE_COMIC = 6;
    public static final int UN_FAVORITE_COMIC = 7;

    private int type;
    private Object data;
    private Object second;

    public EventMessage(int type, Object data) {
        this.type = type;
        this.data = data;
        this.second = null;
    }

    public EventMessage(int type, Object data, Object second) {
        this.type = type;
        this.data = data;
        this.second = second;
    }


    public int getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public Object getSecond() {
        return second;
    }

}
