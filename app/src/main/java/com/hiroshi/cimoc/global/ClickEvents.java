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

    public enum JoyLocks {
        LT,
        RT
    }

    public static String[] getPageClickEvents() {
        return new String[]{PreferenceManager.PREF_READER_PAGE_CLICK_LEFT, PreferenceManager.PREF_READER_PAGE_CLICK_TOP,
                PreferenceManager.PREF_READER_PAGE_CLICK_MIDDLE, PreferenceManager.PREF_READER_PAGE_CLICK_BOTTOM,
                PreferenceManager.PREF_READER_PAGE_CLICK_RIGHT, PreferenceManager.PREF_READER_PAGE_CLICK_UP,
                PreferenceManager.PREF_READER_PAGE_CLICK_DOWN};
    }

    public static String[] getPageLongClickEvents() {
        return new String[]{PreferenceManager.PREF_READER_PAGE_LONG_CLICK_LEFT, PreferenceManager.PREF_READER_PAGE_LONG_CLICK_TOP,
                PreferenceManager.PREF_READER_PAGE_LONG_CLICK_MIDDLE, PreferenceManager.PREF_READER_PAGE_LONG_CLICK_BOTTOM,
                PreferenceManager.PREF_READER_PAGE_LONG_CLICK_RIGHT};
    }

    public static int[] getPageClickEventChoice(PreferenceManager manager) {
        final int[] array = {
                //screen
                manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_LEFT, EVENT_PREV_PAGE),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_TOP, EVENT_PREV_PAGE),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_MIDDLE, EVENT_SWITCH_CONTROL),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_BOTTOM, EVENT_NEXT_PAGE),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_RIGHT, EVENT_NEXT_PAGE),
                //key
                manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_UP, EVENT_PREV_PAGE),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_CLICK_DOWN, EVENT_NEXT_PAGE),
                //joy
                manager.getInt(PreferenceManager.PREF_READER_PAGE_JOY_LT, EVENT_PREV_PAGE),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_JOY_RT, EVENT_NEXT_PAGE),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_JOY_LEFT, EVENT_PREV_PAGE),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_JOY_RIGHT, EVENT_NEXT_PAGE),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_JOY_UP, EVENT_LOAD_PREV),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_JOY_DOWN, EVENT_LOAD_NEXT),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_JOY_B, EVENT_EXIT_READER),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_JOY_A, EVENT_NULL),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_JOY_X, EVENT_SWITCH_CONTROL),
                manager.getInt(PreferenceManager.PREF_READER_PAGE_JOY_Y, EVENT_SAVE_PICTURE),
        };
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
        return new String[]{
                //screen
                PreferenceManager.PREF_READER_STREAM_CLICK_LEFT,
                PreferenceManager.PREF_READER_STREAM_CLICK_TOP,
                PreferenceManager.PREF_READER_STREAM_CLICK_MIDDLE,
                PreferenceManager.PREF_READER_STREAM_CLICK_BOTTOM,
                PreferenceManager.PREF_READER_STREAM_CLICK_RIGHT,
                //key
                PreferenceManager.PREF_READER_STREAM_CLICK_UP,
                PreferenceManager.PREF_READER_STREAM_CLICK_DOWN,
                //joy
                PreferenceManager.PREF_READER_PAGE_JOY_LT,
                PreferenceManager.PREF_READER_PAGE_JOY_RT,
                PreferenceManager.PREF_READER_PAGE_JOY_LEFT,
                PreferenceManager.PREF_READER_PAGE_JOY_RIGHT,
                PreferenceManager.PREF_READER_PAGE_JOY_UP,
                PreferenceManager.PREF_READER_PAGE_JOY_DOWN,
                PreferenceManager.PREF_READER_PAGE_JOY_B,
                PreferenceManager.PREF_READER_PAGE_JOY_A,
                PreferenceManager.PREF_READER_PAGE_JOY_X,
                PreferenceManager.PREF_READER_PAGE_JOY_Y,
        };
    }

    public static String[] getStreamLongClickEvents() {
        return new String[]{PreferenceManager.PREF_READER_STREAM_LONG_CLICK_LEFT, PreferenceManager.PREF_READER_STREAM_LONG_CLICK_TOP,
                PreferenceManager.PREF_READER_STREAM_LONG_CLICK_MIDDLE, PreferenceManager.PREF_READER_STREAM_LONG_CLICK_BOTTOM,
                PreferenceManager.PREF_READER_STREAM_LONG_CLICK_RIGHT};
    }

    public static int[] getStreamClickEventChoice(PreferenceManager manager) {
        final int[] array = {
                //screen
                manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_LEFT, EVENT_NULL),//0
                manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_TOP, EVENT_NULL),//1
                manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_MIDDLE, EVENT_SWITCH_CONTROL),//2
                manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_BOTTOM, EVENT_NULL),//3
                manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_RIGHT, EVENT_NULL),//4
                //key
                manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_UP, EVENT_NULL),//5
                manager.getInt(PreferenceManager.PREF_READER_STREAM_CLICK_DOWN, EVENT_NULL),//6
                //joy
                manager.getInt(PreferenceManager.PREF_READER_STREAM_JOY_LT, EVENT_PREV_PAGE),//7
                manager.getInt(PreferenceManager.PREF_READER_STREAM_JOY_RT, EVENT_NEXT_PAGE),//8
                manager.getInt(PreferenceManager.PREF_READER_STREAM_JOY_LEFT, EVENT_NULL),//9
                manager.getInt(PreferenceManager.PREF_READER_STREAM_JOY_RIGHT, EVENT_NULL),//10
                manager.getInt(PreferenceManager.PREF_READER_STREAM_JOY_UP, EVENT_NULL),//11
                manager.getInt(PreferenceManager.PREF_READER_STREAM_JOY_DOWN, EVENT_NULL),//12
                manager.getInt(PreferenceManager.PREF_READER_STREAM_JOY_B, EVENT_NULL),//13
                manager.getInt(PreferenceManager.PREF_READER_STREAM_JOY_A, EVENT_NULL),//14
                manager.getInt(PreferenceManager.PREF_READER_STREAM_JOY_X, EVENT_SWITCH_CONTROL),//15
                manager.getInt(PreferenceManager.PREF_READER_STREAM_JOY_Y, EVENT_SAVE_PICTURE),//16
        };
        return array;
    }

    public static int[] getStreamLongClickEventChoice(PreferenceManager manager) {
        int[] array = new int[7];
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
