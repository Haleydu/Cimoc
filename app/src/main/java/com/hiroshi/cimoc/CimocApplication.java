package com.hiroshi.cimoc;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.model.DaoMaster;
import com.hiroshi.cimoc.model.DaoMaster.DevOpenHelper;
import com.hiroshi.cimoc.model.DaoSession;
import com.hiroshi.cimoc.utils.ImagePipelineConfigFactory;

import org.greenrobot.greendao.database.Database;

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
        DevOpenHelper helper = new DevOpenHelper(this, "cimoc.db");
        Database db = helper.getWritableDb();
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
