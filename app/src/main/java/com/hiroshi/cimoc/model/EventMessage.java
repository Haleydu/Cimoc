package com.hiroshi.cimoc.model;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
    public static final int COMIC_DELETE = 15;

    @IntDef({SEARCH_SUCCESS, SEARCH_FAIL, LOAD_COMIC_SUCCESS, LOAD_COMIC_FAIL, PARSE_PIC_SUCCESS, PARSE_PIC_FAIL,
            NETWORK_ERROR, FAVORITE_COMIC, UN_FAVORITE_COMIC, HISTORY_COMIC, COMIC_PAGE_CHANGE, COMIC_LAST_CHANGE,
            DELETE_HISTORY, RESTORE_FAVORITE, COMIC_DELETE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {}

    private int type;
    private Object data;
    private Object second;

    public EventMessage(@EventType int type, Object data) {
        this.type = type;
        this.data = data;
    }

    public EventMessage(@EventType int type, Object data, Object second) {
        this.type = type;
        this.data = data;
        this.second = second;
    }

    public @EventType int getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public Object getSecond() { return second; }

}
