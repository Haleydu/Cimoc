package com.hiroshi.cimoc.global;

import android.content.Context;

import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.manager.PreferenceManager;

/**
 * Created by Hiroshi on 2016/10/9.
 */

public class ClickEvents {

    public static final int EVENT_NULL = 0;
    public static final int EVENT_PREV_PAGE = 1;
    public static final int EVENT_NEXT_PAGE = 2;
    public static final int EVENT_SAVE_PICTURE = 3;
    public static final int EVENT_LOAD_PREV = 4;
    public static final int EVENT_LOAD_NEXT = 5;
    public static final int EVENT_EXIT_READER = 6;
    public static final int EVENT_TO_FIRST = 7;
    public static final int EVENT_TO_LAST = 8;
    public static final int EVENT_SWITCH_SCREEN = 9;
    public static final int EVENT_SWITCH_MODE = 10;
    public static final int EVENT_SWITCH_CONTROL = 11;
    public static final int EVENT_RELOAD_IMAGE = 12;
    public static final int EVENT_SWITCH_NIGHT = 13;

    private static String[] mEventTitle;

    public static String[] getPageClickEvents() {
        return new String[] { PreferenceManager.PREF_READER_PAGE_CLICK_LEFT, PreferenceManager.PREF_READER_PAGE_CLICK_TOP,
                PreferenceManager.PREF_READER_PAGE_CLICK_MIDDLE, PreferenceManager.PREF_READER_PAGE_CLICK_BOTTOM,
                PreferenceManager.PREF_READER_PAGE_CLICK_RIGHT, PreferenceManager.PREF_READER_PAGE_CLICK_UP,
                PreferenceManager.PREF_READER_PAGE_CLICK_DOWN };
    }

    public static String[] getPageLongClickEvents() {
        return new String[] { PreferenceManager.PREF_READER_PAGE_LONG_CLICK_LEFT, PreferenceManager.PREF_READER_PAGE_LONG_CLICK_TOP,
                PreferenceManager.PREF_READER_PAGE_LONG_CLICK_MIDDLE, PreferenceManager.PREF_READER_PAGE_LONG_CLICK_BOTTOM,
                PreferenceManager.PREF_READER_PAGE_LONG_CLICK_RIGHT };
    }

    public static int[] getPageClickEventChoice(PreferenceManager manager) {
        int[] array = new int[7];
        array[0] = manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_LEFT, 0);
        array[1] = manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_TOP, 0);
        array[2] = manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_MIDDLE, EVENT_SWITCH_CONTROL);
        array[3] = manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_BOTTOM, 0);
        array[4] = manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_RIGHT, 0);
        array[5] = manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_UP, 0);
        array[6] = manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_DOWN, 0);
        return array;
    }

    public static int[] getPageLongClickEventChoice(PreferenceManager manager) {
        int[] array = new int[5];
        array[0] = manager.getInt(PreferenceManager.PREF_READER_PAGE_LONG_CLICK_LEFT, 0);
        array[1] = manager.getInt(PreferenceManager.PREF_READER_PAGE_LONG_CLICK_TOP, 0);
        array[2] = manager.getInt(PreferenceManager.PREF_READER_PAGE_LONG_CLICK_MIDDLE, 0);
        array[3] = manager.getInt(PreferenceManager.PREF_READER_PAGE_LONG_CLICK_BOTTOM, 0);
        array[4] = manager.getInt(PreferenceManager.PREF_READER_PAGE_LONG_CLICK_RIGHT, 0);
        return array;
    }

    public static String[] getStreamClickEvents() {
        return new String[] { PreferenceManager.PREF_READER_STREAM_CLICK_LEFT, PreferenceManager.PREF_READER_STREAM_CLICK_TOP,
                PreferenceManager.PREF_READER_STREAM_CLICK_MIDDLE, PreferenceManager.PREF_READER_STREAM_CLICK_BOTTOM,
                PreferenceManager.PREF_READER_STREAM_CLICK_RIGHT, PreferenceManager.PREF_READER_STREAM_CLICK_UP,
                PreferenceManager.PREF_READER_STREAM_CLICK_DOWN };
    }

    public static String[] getStreamLongClickEvents() {
        return new String[] { PreferenceManager.PREF_READER_STREAM_LONG_CLICK_LEFT, PreferenceManager.PREF_READER_STREAM_LONG_CLICK_TOP,
                PreferenceManager.PREF_READER_STREAM_LONG_CLICK_MIDDLE, PreferenceManager.PREF_READER_STREAM_LONG_CLICK_BOTTOM,
                PreferenceManager.PREF_READER_STREAM_LONG_CLICK_RIGHT };
    }

    public static int[] getStreamClickEventChoice(PreferenceManager manager) {
        int[] array = new int[7];
        array[0] = manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_LEFT, 0);
        array[1] = manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_TOP, 0);
        array[2] = manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_MIDDLE, EVENT_SWITCH_CONTROL);
        array[3] = manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_BOTTOM, 0);
        array[4] = manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_RIGHT, 0);
        array[5] = manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_UP, 0);
        array[6] = manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_DOWN, 0);
        return array;
    }

    public static int[] getStreamLongClickEventChoice(PreferenceManager manager) {
        int[] array = new int[5];
        array[0] = manager.getInt(PreferenceManager.PREF_READER_STREAM_LONG_CLICK_LEFT, 0);
        array[1] = manager.getInt(PreferenceManager.PREF_READER_STREAM_LONG_CLICK_TOP, 0);
        array[2] = manager.getInt(PreferenceManager.PREF_READER_STREAM_LONG_CLICK_MIDDLE, 0);
        array[3] = manager.getInt(PreferenceManager.PREF_READER_STREAM_LONG_CLICK_BOTTOM, 0);
        array[4] = manager.getInt(PreferenceManager.PREF_READER_STREAM_LONG_CLICK_RIGHT, 0);
        return array;
    }

    public static String[] getEventTitleArray(Context context) {
        if (mEventTitle == null) {
            mEventTitle = context.getResources().getStringArray(R.array.event_items);
        }
        return mEventTitle;
    }

    public static String getEventTitle(Context context, int value) {
        if (mEventTitle == null) {
            mEventTitle = context.getResources().getStringArray(R.array.event_items);
        }
        return mEventTitle[value];
    }

}
