package com.hiroshi.cimoc.model;

/**
 * Created by Hiroshi on 2016/7/2.
 */
public class EventMessage {

    public static final int SEARCH_SUCCESS = 1;
    public static final int SEARCH_FAIL = 2;
    public static final int LOAD_COMIC_SUCCESS = 3;
    public static final int LOAD_COMIC_FAIL = 4;
    public static final int PARSE_PIC_SUCCESS = 5;
    public static final int PARSE_PIC_FAIL = 6;
    public static final int NETWORK_ERROR = 7;
    public static final int FAVORITE_COMIC = 8;
    public static final int UN_FAVORITE_COMIC = 9;
    public static final int HISTORY_COMIC = 10;
    public static final int COMIC_PAGE_CHANGE = 11;
    public static final int COMIC_LAST_CHANGE = 12;
    public static final int DELETE_HISTORY = 13;
    public static final int RESTORE_FAVORITE = 14;

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
