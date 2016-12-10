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
import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.fresco.ControllerBuilderProvider;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.DaoMaster;
import com.hiroshi.cimoc.model.DaoSession;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.model.Task;
import com.hiroshi.cimoc.model.TaskDao;
import com.hiroshi.cimoc.ui.adapter.GridAdapter;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        update();
        mContentResolver = getContentResolver();
        initRootDocumentFile();
        Fresco.initialize(this);
    }

    public void update() {
        int version = mPreferenceManager.getInt(PreferenceManager.PREF_APP_VERSION, 0);
        if (version != VERSION) {
            switch (version) {
                case 0:
                    updateSource();
                    mDaoSession.runInTx(new Runnable() {
                        @Override
                        public void run() {
                            updateTaskPath();
                        }
                    });
            }
            mPreferenceManager.putInt(PreferenceManager.PREF_APP_VERSION, VERSION);
        }
    }

    private void updateSource() {
        int[] type = { SourceManager.SOURCE_IKANMAN, SourceManager.SOURCE_DMZJ, SourceManager.SOURCE_HHAAZZ, SourceManager.SOURCE_CCTUKU,
                SourceManager.SOURCE_U17, SourceManager.SOURCE_DM5, SourceManager.SOURCE_WEBTOON, SourceManager.SOURCE_HHSSEE,
                SourceManager.SOURCE_57MH, SourceManager.SOURCE_CHUIYAO, SourceManager.SOURCE_JMYDM};
        List<Source> list = new ArrayList<>(type.length);
        for (int i = 0; i != type.length; ++i) {
            list.add(new Source(null, SourceManager.getTitle(type[i]), type[i], true));
        }
        mDaoSession.getSourceDao().insertOrReplaceInTx(list);
    }

    private void updateTaskPath() {
        // 更新汗汗漫画章节路径
        List<Comic> list = mDaoSession.getComicDao().queryBuilder().where(ComicDao.Properties.Source.eq(7)).list();
        TaskDao dao = mDaoSession.getTaskDao();
        for (Comic comic : list) {
            List<Task> tasks = dao.queryBuilder().where(TaskDao.Properties.Key.eq(comic.getId())).list();
            for (Task task : tasks) {
                String path = task.getPath();
                String[] args = path.split(" ");
                task.setPath(args[0].concat("-").concat(args[1]));
                dao.update(task);
            }
        }
    }

    public void initRootDocumentFile() {
        String uri = mPreferenceManager.getString(PreferenceManager.PREF_OTHER_STORAGE);
        if (uri == null) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cimoc");
            if (file.exists() || file.mkdirs()) {
                mDocumentFile = DocumentFile.fromFile(file);
            } else {
                mDocumentFile = null;
            }
        } else if (uri.startsWith("content")) {
            mDocumentFile = DocumentFile.fromTreeUri(this, Uri.parse(uri));
        } else if (uri.startsWith("file")) {
            mDocumentFile = DocumentFile.fromFile(new File(Uri.parse(uri).getPath()));
        } else {
            mDocumentFile = DocumentFile.fromFile(new File(uri, "Cimoc"));
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
