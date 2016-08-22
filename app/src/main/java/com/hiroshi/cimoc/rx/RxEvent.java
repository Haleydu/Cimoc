package com.hiroshi.cimoc.rx;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public class RxEvent {

    public static final int NETWORK_ERROR = 1;
    public static final int FAVORITE_COMIC = 2;
    public static final int UN_FAVORITE_COMIC = 3;
    public static final int HISTORY_COMIC = 4;
    public static final int COMIC_PAGE_CHANGE = 5;
    public static final int COMIC_CHAPTER_CHANGE = 6;
    public static final int DELETE_HISTORY = 7;
    public static final int RESTORE_FAVORITE = 8;
    public static final int COMIC_DELETE = 9;
    public static final int IMAGE_LAZY_LOAD = 10;

    @IntDef({NETWORK_ERROR, FAVORITE_COMIC, UN_FAVORITE_COMIC, HISTORY_COMIC, COMIC_PAGE_CHANGE, COMIC_CHAPTER_CHANGE,
            DELETE_HISTORY, RESTORE_FAVORITE, COMIC_DELETE, IMAGE_LAZY_LOAD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {}

    private int type;
    private Object[] data;

    public RxEvent(@EventType int type, Object... data) {
        this.type = type;
        this.data = data;
    }

    public @EventType int getType() {
        return type;
    }

    public Object getData() {
        return getData(0);
    }

    public Object getData(int index) {
        return data[index];
    }

}
