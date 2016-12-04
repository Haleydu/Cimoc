package com.hiroshi.cimoc;

import android.app.Application;
import android.content.ContentResolver;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.core.DBOpenHelper;
import com.hiroshi.cimoc.core.Storage;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.DaoMaster;
import com.hiroshi.cimoc.model.DaoSession;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;
import com.hiroshi.cimoc.utils.FileUtils;

import org.greenrobot.greendao.identityscope.IdentityScopeType;

import okhttp3.OkHttpClient;

/**
 * Created by Hiroshi on 2016/7/5.
 */
public class CimocApplication extends Application {

    // 1.04.04.000
    public static final int VERSION = 10404000;

    private static String mStorageUri;
    private static DaoSession daoSession;
    private static OkHttpClient httpClient;
    private static ContentResolver mContentResolver;

    private PreferenceManager mPreferenceManager;
    private ControllerBuilderProvider mBuilderProvider;
    private RecyclerView.RecycledViewPool mRecycledPool;

    @Override
    public void onCreate() {
        super.onCreate();
        DBOpenHelper helper = new DBOpenHelper(this, "cimoc.db");
        httpClient = new OkHttpClient();
        daoSession = new DaoMaster(helper.getWritableDatabase()).newSession(IdentityScopeType.None);
        mPreferenceManager = new PreferenceManager(getApplicationContext());
        mStorageUri = mPreferenceManager.getString(PreferenceManager.PREF_OTHER_STORAGE);
        Storage.STORAGE_DIR = FileUtils.getPath(mPreferenceManager.getString(PreferenceManager.PREF_OTHER_STORAGE,
                Environment.getExternalStorageDirectory().getAbsolutePath()), "Cimoc");
        //String uri = "content://com.android.externalstorage.documents/tree/primary%3ACimoc";
        mContentResolver = getContentResolver();
        Fresco.initialize(this);
    }

    public static String getStorageUri() {
        return mStorageUri;
    }

    public static ContentResolver getResolver() {
        return mContentResolver;
    }

    public static DaoSession getDaoSession() {
        return daoSession;
    }

    public static OkHttpClient getHttpClient() {
        return httpClient;
    }

    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    public RecyclerView.RecycledViewPool getGridRecycledPool() {
        if (mRecycledPool == null) {
            mRecycledPool = new RecyclerView.RecycledViewPool();
            mRecycledPool.setMaxRecycledViews(GridAdapter.GRID_ITEM_TYPE, 20);
        }
        return mRecycledPool;
    }

    public ControllerBuilderProvider getBuilderProvider() {
        if (mBuilderProvider == null) {
            mBuilderProvider = new ControllerBuilderProvider(getApplicationContext());
        }
        return mBuilderProvider;
    }

}
