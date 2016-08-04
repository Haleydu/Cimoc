package com.hiroshi.cimoc.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.hiroshi.cimoc.R;

/**
 * Created by Hiroshi on 2016/8/4.
 */
public class PreferenceMaster {

    public static final String PREF_EX = "pref_ex";
    public static final String PREF_HOME = "pref_home";
    public static final String PREF_READER = "pref_reader";

    private static final String PREFERENCES_NAME = "cimoc_preferences";

    private SharedPreferences mSharedPreferences;

    public PreferenceMaster(Context context) {
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

    public static String getTitleById(int id) {
        switch (id) {
            default:
            case R.id.drawer_cimoc:
                return "Cimoc";
            case R.id.drawer_favorite:
                return "我的漫画";
            case R.id.drawer_history:
                return "历史阅读";
        }
    }

}
