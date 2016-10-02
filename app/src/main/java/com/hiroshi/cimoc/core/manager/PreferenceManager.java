package com.hiroshi.cimoc.core.manager;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Hiroshi on 2016/8/4.
 */
public class PreferenceManager {

    public static final int MODE_HORIZONTAL_PAGE = 0;
    public static final int MODE_PORTRAIT_STREAM = 1;
    public static final int MODE_LANDSCAPE_STREAM = 2;

    public static final int HOME_CIMOC = 0;
    public static final int HOME_FAVORITE = 1;
    public static final int HOME_HISTORY = 2;
    public static final int HOME_DOWNLOAD = 3;
    public static final int HOME_SOURCE = 4;

    public static final String PREF_HOME = "pref_home";
    public static final String PREF_THEME = "pref_theme";
    public static final String PREF_MODE = "pref_mode";
    public static final String PREF_VOLUME = "pref_volume";
    public static final String PREF_NIGHT = "pref_night";
    public static final String PREF_SPLIT = "pref_split";
    public static final String PREF_REVERSE = "pref_reverse";
    public static final String PREF_PICTURE = "pref_picture";
    public static final String PREF_BRIGHT = "pref_bright";
    public static final String PREF_HIDE = "pref_hide";
    public static final String PREF_TRIGGER = "pref_trigger";
    public static final String PREF_BLANK = "pref_blank";

    private static final String PREFERENCES_NAME = "cimoc_preferences";

    private SharedPreferences mSharedPreferences;

    public PreferenceManager(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public String getString(String key, String defValue) {
        return mSharedPreferences.getString(key, defValue);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return mSharedPreferences.getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return mSharedPreferences.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return mSharedPreferences.getLong(key, defValue);
    }

    public void putString(String key, String value) {
        mSharedPreferences.edit().putString(key, value).apply();
    }

    public void putBoolean(String key, boolean value) {
        mSharedPreferences.edit().putBoolean(key, value).apply();
    }

    public void putInt(String key, int value) {
        mSharedPreferences.edit().putInt(key, value).apply();
    }

    public void putLong(String key, long value) {
        mSharedPreferences.edit().putLong(key, value).apply();
    }

}
