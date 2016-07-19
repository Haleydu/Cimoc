package com.hiroshi.cimoc;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.utils.ImagePipelineConfigFactory;
import com.hiroshi.db.dao.DaoMaster;
import com.hiroshi.db.dao.DaoSession;

import okhttp3.OkHttpClient;

/**
 * Created by Hiroshi on 2016/7/5.
 */
public class CimocApplication extends Application {

    private static DaoSession daoSession;
    private static OkHttpClient httpClient;
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        initDatabase();
        context = getApplicationContext();
        Fresco.initialize(context, ImagePipelineConfigFactory.getImagePipelineConfig(context));
    }

    private void initDatabase() {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "cimoc.db", null);
        SQLiteDatabase db = helper.getWritableDatabase();
        daoSession = new DaoMaster(db).newSession();

    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }

    public static OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient();
        }
        return httpClient;
    }

    public static Context getContext() {
        return context;
    }

}
