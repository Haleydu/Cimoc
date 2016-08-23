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
        switch (oldVersion) {
            case 1:
                SourceDao.createTable(db, false);
                initSource(db);
            case 2:
                updateHighlight(db);
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

    private void updateHighlight(Database db) {
        db.beginTransaction();
        db.execSQL("CREATE TABLE \"COMIC2\" (" +
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "\"SOURCE\" INTEGER NOT NULL ," +
                "\"CID\" TEXT NOT NULL ," +
                "\"TITLE\" TEXT NOT NULL ," +
                "\"COVER\" TEXT NOT NULL ," +
                "\"UPDATE\" TEXT NOT NULL ," +
                "\"HIGHLIGHT\" INTEGER NOT NULL ," +
                "\"FAVORITE\" INTEGER," +
                "\"HISTORY\" INTEGER," +
                "\"LAST\" TEXT," +
                "\"PAGE\" INTEGER)");
        db.execSQL("INSERT INTO \"COMIC2\" (\"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", \"UPDATE\", \"HIGHLIGHT\", \"FAVORITE\", \"HISTORY\", \"LAST\", \"PAGE\")" +
                " SELECT \"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", \"UPDATE\", 0, \"FAVORITE\", \"HISTORY\", \"LAST\", \"PAGE\" FROM \"COMIC\"");
        db.execSQL("DROP TABLE \"COMIC\"");
        db.execSQL("ALTER TABLE \"COMIC2\" RENAME TO \"COMIC\"");
        db.execSQL("UPDATE \"COMIC\" SET \"HIGHLIGHT\" = 1, \"FAVORITE\" = " + System.currentTimeMillis() + " WHERE \"FAVORITE\" = " + 0xFFFFFFFFFFFL);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}
