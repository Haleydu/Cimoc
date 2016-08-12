package com.hiroshi.cimoc;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.model.DaoMaster;
import com.hiroshi.cimoc.model.DaoSession;
import com.hiroshi.cimoc.core.DBOpenHelper;
import com.hiroshi.cimoc.core.PreferenceMaster;

import okhttp3.OkHttpClient;

/**
 * Created by Hiroshi on 2016/7/5.
 */
public class CimocApplication extends Application {

    private static DaoSession daoSession;
    private static OkHttpClient httpClient;
    private static PreferenceMaster preferences;

    @Override
    public void onCreate() {
        super.onCreate();
        DBOpenHelper helper = new DBOpenHelper(this, "cimoc.db");
        daoSession = new DaoMaster(helper.getWritableDatabase()).newSession();
        preferences = new PreferenceMaster(getApplicationContext());
        Fresco.initialize(this);
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }

    public static PreferenceMaster getPreferences() {
        return preferences;
    }

    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient();
        }
        return httpClient;
    }

}
