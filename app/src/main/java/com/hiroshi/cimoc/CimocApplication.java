package com.hiroshi.cimoc;

import android.app.Application;
import android.os.Environment;
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

import java.io.File;

import okhttp3.OkHttpClient;

/**
 * Created by Hiroshi on 2016/7/5.
 */
public class CimocApplication extends Application {

    private static DaoSession daoSession;
    private static OkHttpClient httpClient;

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
        Storage.STORAGE_DIR = FileUtils.getPath(mPreferenceManager.getString(PreferenceManager.PREF_OTHER_STORAGE,
                Environment.getExternalStorageDirectory().getAbsolutePath()), "Cimoc");
        renameDownload();
        Fresco.initialize(this);
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

    private void renameDownload() {
        try {
            for (File sourceDir : FileUtils.listFiles(FileUtils.getPath(Storage.STORAGE_DIR, "download"))) {
                if (sourceDir.isDirectory()) {
                    for (File comicDir : FileUtils.listFiles(sourceDir)) {
                        if (comicDir.isDirectory()) {
                            String filter = FileUtils.filterFilename(comicDir.getAbsolutePath());
                            if (!filter.equals(comicDir.getAbsolutePath())) {
                                FileUtils.rename(comicDir.getAbsolutePath(), filter);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
