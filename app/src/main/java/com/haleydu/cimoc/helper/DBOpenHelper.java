package com.haleydu.cimoc.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.haleydu.cimoc.model.ChapterDao;
import com.haleydu.cimoc.model.ComicDao;
import com.haleydu.cimoc.model.DaoMaster;
import com.haleydu.cimoc.model.ImageUrlDao;
import com.haleydu.cimoc.model.SourceDao;
import com.haleydu.cimoc.model.TagDao;
import com.haleydu.cimoc.model.TagRefDao;
import com.haleydu.cimoc.model.TaskDao;
import com.haleydu.cimoc.utils.StringUtils;

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
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        Log.d("DB", StringUtils.format("DB V:%d,%d", oldVersion, newVersion));
        switch (oldVersion) {
            case 1:
                SourceDao.createTable(db, false);
            case 2:
                updateHighlight(db);
            case 3:
                TaskDao.createTable(db, false);
                updateDownload(db);
            case 4:
            case 5:
            case 6:
                SourceDao.dropTable(db, false);
                SourceDao.createTable(db, false);
                TagDao.createTable(db, false);
                TagRefDao.createTable(db, false);
            case 7:
            case 8:
                updateLocal(db);
            case 9:
                updateSource(db);
            case 10:
                updateIntroAndAuthor(db);
                ChapterDao.createTable(db, true);
                ImageUrlDao.createTable(db, true);
            case 11:
                updateChapter(db);

        }
    }

    private void updateChapter(Database db) {
        db.beginTransaction();
        db.execSQL("ALTER TABLE \"CHAPTER\" RENAME TO \"CHAPTER2\"");
        ChapterDao.createTable(db, false);
        db.execSQL("INSERT INTO \"CHAPTER\" (\"_id\", \"SOURCE_COMIC\", \"TITLE\", \"PATH\", \"COUNT\", \"COMPLETE\", \"DOWNLOAD\",  \"TID\" , \"SOURCE_GROUP\")" +
                " SELECT \"_id\", \"SOURCE_COMIC\", \"TITLE\", \"PATH\", \"COUNT\", \"COMPLETE\", \"DOWNLOAD\", \"TID\",\"\" FROM \"CHAPTER2\"");
        db.execSQL("DROP TABLE \"CHAPTER2\"");
        db.setTransactionSuccessful();
        db.endTransaction();


    }

    private void updateLocal(Database db) {
        db.beginTransaction();
        db.execSQL("ALTER TABLE \"COMIC\" RENAME TO \"COMIC2\"");
        ComicDao.createTable(db, false);
        db.execSQL("INSERT INTO \"COMIC\" (\"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", " +
                "\"UPDATE\", \"HIGHLIGHT\", \"LOCAL\", \"FAVORITE\", \"HISTORY\", \"DOWNLOAD\", " +
                "\"LAST\",  \"PAGE\", \"CHAPTER\") SELECT \"_id\", \"SOURCE\", \"CID\", \"TITLE\", " +
                "\"COVER\", \"UPDATE\", \"HIGHLIGHT\", 0, \"FAVORITE\", \"HISTORY\", \"DOWNLOAD\", " +
                "\"LAST\",  \"PAGE\", null FROM \"COMIC2\"");
        db.execSQL("DROP TABLE \"COMIC2\"");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void updateIntroAndAuthor(Database db) {
        db.beginTransaction();
        db.execSQL("ALTER TABLE \"COMIC\" RENAME TO \"COMIC2\"");
        ComicDao.createTable(db, false);
        db.execSQL("INSERT INTO \"COMIC\" (\"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", " +
                "\"UPDATE\", \"HIGHLIGHT\", \"LOCAL\", \"FAVORITE\", \"HISTORY\", \"DOWNLOAD\", " +
                "\"LAST\",  \"PAGE\", \"CHAPTER\", \"INTRO\", \"AUTHOR\") SELECT \"_id\", \"SOURCE\", \"CID\", \"TITLE\", " +
                "\"COVER\", \"UPDATE\", \"HIGHLIGHT\", \"LOCAL\", \"FAVORITE\", \"HISTORY\", \"DOWNLOAD\", " +
                "\"LAST\",  \"PAGE\", \"CHAPTER\" ,null,null FROM \"COMIC2\"");
        db.execSQL("DROP TABLE \"COMIC2\"");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void updateSource(Database db) {
        db.beginTransaction();
        db.execSQL("ALTER TABLE \"SOURCE\" RENAME TO \"SOURCE2\"");
        SourceDao.createTable(db, false);
        db.execSQL("INSERT INTO \"SOURCE\" (\"_id\", \"TYPE\", \"TITLE\", \"ENABLE\")" +
                " SELECT \"_id\", \"TYPE\", \"TITLE\", \"ENABLE\" FROM \"SOURCE2\"");
        db.execSQL("DROP TABLE \"SOURCE2\"");
        db.execSQL("ALTER TABLE \"COMIC\" ADD COLUMN \"URL\" TEXT");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void updateDownload(Database db) {
        db.beginTransaction();
        db.execSQL("ALTER TABLE \"COMIC\" RENAME TO \"COMIC2\"");
        ComicDao.createTable(db, false);
        db.execSQL("INSERT INTO \"COMIC\" (\"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", " +
                "\"HIGHLIGHT\", \"UPDATE\", \"FINISH\", \"FAVORITE\", \"HISTORY\", \"DOWNLOAD\", " +
                "\"LAST\", \"PAGE\")  SELECT \"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", " +
                "\"HIGHLIGHT\", \"UPDATE\", null, \"FAVORITE\", \"HISTORY\", null, \"LAST\", " +
                "\"PAGE\" FROM \"COMIC2\"");
        db.execSQL("DROP TABLE \"COMIC2\"");
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    private void updateHighlight(Database db) {
        db.beginTransaction();
        db.execSQL("ALTER TABLE \"COMIC\" RENAME TO \"COMIC2\"");
        ComicDao.createTable(db, false);
        db.execSQL("INSERT INTO \"COMIC\" (\"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", " +
                "\"UPDATE\", \"HIGHLIGHT\", \"FAVORITE\", \"HISTORY\", \"LAST\", \"PAGE\")" +
                " SELECT \"_id\", \"SOURCE\", \"CID\", \"TITLE\", \"COVER\", \"UPDATE\", 0, " +
                "\"FAVORITE\", \"HISTORY\", \"LAST\", \"PAGE\" FROM \"COMIC2\"");
        db.execSQL("DROP TABLE \"COMIC2\"");
        db.execSQL("UPDATE \"COMIC\" SET \"HIGHLIGHT\" = 1, \"FAVORITE\" = " +
                System.currentTimeMillis() + " WHERE \"FAVORITE\" = " + 0xFFFFFFFFFFFL);
        db.setTransactionSuccessful();
        db.endTransaction();
    }

}
