package com.hiroshi.cimoc.core;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.hiroshi.cimoc.core.manager.SourceManager;
import com.hiroshi.cimoc.model.DaoMaster;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.model.SourceDao;

import org.greenrobot.greendao.database.Database;

/**
 * Created by Hiroshi on 2016/8/12.
 */
public class DBOpenHelper extends DaoMaster.OpenHelper {

    public DBOpenHelper(Context context, String name) {
        super(context, name);
    }

    public DBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
        initSource(db);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        switch (newVersion) {
            case 2:
                SourceDao.createTable(db, false);
                initSource(db);
        }
    }

    private void initSource(Database db) {
        SourceDao dao = new DaoMaster(db).newSession().getSourceDao();
        Source[] array = new Source[5];
        array[0] = new Source(null, SourceManager.SOURCE_IKANMAN, true);
        array[1] = new Source(null, SourceManager.SOURCE_DMZJ, true);
        array[2] = new Source(null, SourceManager.SOURCE_HHAAZZ, true);
        array[3] = new Source(null, SourceManager.SOURCE_CCTUKU, true);
        array[4] = new Source(null, SourceManager.SOURCE_U17, true);
        dao.insertInTx(array);
    }

}
