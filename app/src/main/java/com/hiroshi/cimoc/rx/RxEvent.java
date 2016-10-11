package com.hiroshi.cimoc.rx;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public class RxEvent {

    public static final int FAVORITE_COMIC = 2;
    public static final int UN_FAVORITE_COMIC = 3;
    public static final int HISTORY_COMIC = 4;
    public static final int COMIC_PAGE_CHANGE = 5;
    public static final int COMIC_CHAPTER_CHANGE = 6;
    public static final int RESTORE_FAVORITE = 7;
    public static final int COMIC_DELETE = 8;
    public static final int TASK_STATE_CHANGE = 9;
    public static final int TASK_PROCESS = 10;
    public static final int TASK_ADD = 11;
    public static final int DOWNLOAD_DELETE = 12;
    public static final int DOWNLOAD_START = 13;
    public static final int DOWNLOAD_STOP = 14;
    public static final int COMIC_FILTER = 15;

    @IntDef({FAVORITE_COMIC, UN_FAVORITE_COMIC, HISTORY_COMIC, COMIC_PAGE_CHANGE, COMIC_CHAPTER_CHANGE, RESTORE_FAVORITE,
            COMIC_DELETE, TASK_STATE_CHANGE, TASK_PROCESS, TASK_ADD, DOWNLOAD_DELETE, DOWNLOAD_START, DOWNLOAD_STOP, COMIC_FILTER})
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
        return index < data.length ? data[index] : null;
    }

}
