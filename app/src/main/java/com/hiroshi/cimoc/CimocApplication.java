package com.hiroshi.cimoc;

import android.app.Application;
import android.content.ContentResolver;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;
import android.support.v7.widget.RecyclerView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.hiroshi.cimoc.core.DBOpenHelper;
import com.hiroshi.cimoc.core.manager.PreferenceManager;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.DaoMaster;
import com.hiroshi.cimoc.model.DaoSession;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;

import org.greenrobot.greendao.identityscope.IdentityScopeType;

import java.io.File;

import okhttp3.OkHttpClient;

/**
 * Created by Hiroshi on 2016/7/5.
 */
public class CimocApplication extends Application {

    // 1.04.04.000
    public static final int VERSION = 10404000;

    private static DaoSession mDaoSession;
    private static OkHttpClient mHttpClient;
    private static DocumentFile mDocumentFile;
    private static ContentResolver mContentResolver;

    private PreferenceManager mPreferenceManager;
    private ControllerBuilderProvider mBuilderProvider;
    private RecyclerView.RecycledViewPool mRecycledPool;

    @Override
    public void onCreate() {
        super.onCreate();
        DBOpenHelper helper = new DBOpenHelper(this, "cimoc.db");
        mHttpClient = new OkHttpClient();
        mDaoSession = new DaoMaster(helper.getWritableDatabase()).newSession(IdentityScopeType.None);
        mPreferenceManager = new PreferenceManager(getApplicationContext());
        mContentResolver = getContentResolver();
        initRootDocumentFile();
        Fresco.initialize(this);
    }

    public void initRootDocumentFile() {
        String uri = mPreferenceManager.getString(PreferenceManager.PREF_OTHER_STORAGE);
        if (uri == null) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cimoc");
            if (file.exists() || file.mkdirs()) {
                mDocumentFile = DocumentFile.fromFile(file);
            }
            mDocumentFile = DocumentFile.fromFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
        } else if (uri.startsWith("content")) {
            mDocumentFile = DocumentFile.fromTreeUri(this, Uri.parse(uri));
        } else {
            mDocumentFile = DocumentFile.fromFile(new File(Uri.parse(uri).getPath()));
        }
    }

    public static DocumentFile getDocumentFile() {
        return mDocumentFile;
    }

    public static ContentResolver getResolver() {
        return mContentResolver;
    }

    public static DaoSession getDaoSession() {
        return mDaoSession;
    }

    public static OkHttpClient getHttpClient() {
        return mHttpClient;
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
