package com.hiroshi.cimoc.core;

import android.database.Cursor;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.ComicDao.Properties;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.model.MiniComic;
import com.hiroshi.cimoc.utils.ExLog;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/9.
 */
public class ComicManager {

    private static ComicManager mComicManager;

    private ComicDao mComicDao;

    private ComicManager() {
        mComicDao = CimocApplication.getDaoSession().getComicDao();
    }

    public void restoreFavorite(final List<Comic> list) {
        mComicDao.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                List<MiniComic> result = new LinkedList<>();
                for (Comic comic : list) {
                    ExLog.d("ComicManager", "get " + comic.getTitle());
                    Comic temp = mComicDao.queryBuilder()
                            .where(Properties.Source.eq(comic.getSource()), Properties.Cid.eq(comic.getCid()))
                            .unique();
                    if (temp == null) {
                        ExLog.d("ComicManager", "insert " + comic.getTitle());
                        comic.setFavorite(System.currentTimeMillis());
                        long id = mComicDao.insert(comic);
                        comic.setId(id);
                        result.add(0, new MiniComic(comic));
                    } else if (temp.getFavorite() == null) {
                        ExLog.d("ComicManager", "update " + comic.getTitle());
                        temp.setFavorite(System.currentTimeMillis());
                        mComicDao.update(temp);
                        result.add(0, new MiniComic(temp));
                    }
                }
                EventBus.getDefault().post(new EventMessage(EventMessage.RESTORE_FAVORITE, result));
            }
        });
    }

    public void cleanHistory() {
        mComicDao.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                List<Comic> list = mComicDao.queryBuilder().where(Properties.History.isNotNull()).list();
                for (Comic comic : list) {
                    if (comic.getFavorite() != null) {
                        comic.setHistory(null);
                        mComicDao.update(comic);
                    } else {
                        mComicDao.delete(comic);
                    }
                }
                EventBus.getDefault().post(new EventMessage(EventMessage.DELETE_HISTORY, list.size()));
            }
        });
    }

    public List<Comic> listBackup() {
        return mComicDao.queryBuilder().where(Properties.Favorite.isNotNull()).list();
    }

    public List<MiniComic> listFavorite() {
        Cursor cursor = mComicDao.queryBuilder()
                .where(ComicDao.Properties.Favorite.isNotNull())
                .orderDesc(Properties.Favorite)
                .buildCursor()
                .query();
        return listByCursor(cursor);
    }

    public List<MiniComic> listHistory() {
        Cursor cursor = mComicDao.queryBuilder()
                .where(Properties.History.isNotNull())
                .orderDesc(Properties.History)
                .buildCursor()
                .query();
        return listByCursor(cursor);
    }

    private List<MiniComic> listByCursor(Cursor cursor) {
        List<MiniComic> list = new LinkedList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(0);
            int source = cursor.getInt(1);
            String cid = cursor.getString(2);
            String title = cursor.getString(3);
            String cover = cursor.getString(4);
            String update = cursor.getString(5);
            list.add(new MiniComic(id, source, cid, title, cover, update));
        }
        cursor.close();
        return list;
    }

    private Comic comic;
    private List<Chapter> chapters;
    private boolean isHistory;

    public void setComic(Long id, int source, String cid) {
        isHistory = false;
        if (id == null) {
            comic = mComicDao.queryBuilder()
                    .where(Properties.Source.eq(source), Properties.Cid.eq(cid))
                    .unique();
        } else {
            comic = mComicDao.load(id);
        }
        if (comic == null) {
            comic = new Comic(source, cid);
        }
        ExLog.d("ComicManager", "init id = " + comic.getId() + " source = " + comic.getSource() + " cid = " + comic.getCid());
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
        ExLog.d("ComicManager", "set chapter list and the number is " + chapters.size());
    }

    public void setLast(String path) {
        comic.setLast(path);
        comic.setPage(1);
        comic.setHistory(System.currentTimeMillis());
        if (!isComicExist()) {
            long id = mComicDao.insert(comic);
            comic.setId(id);
            ExLog.d("ComicManager", "insert " + comic.getTitle() + " to " + comic.getId());
        }
        if (!isHistory) {
            EventBus.getDefault().post(new EventMessage(EventMessage.HISTORY_COMIC, new MiniComic(comic)));
            isHistory = true;
        }
        ExLog.d("ComicManager", "set the last path of" + " [" + comic.getId() + "] " + comic.getTitle() + " to " + path);
    }

    public void afterRead(int page) {
        comic.setPage(page);
        ExLog.d("ComicManager", "set the last page of" + " [" + comic.getId() + "] " + comic.getTitle() + " to " + page);
        EventBus.getDefault().post(new EventMessage(EventMessage.AFTER_READ, comic.getLast()));
    }

    public void favoriteComic() {
        comic.setFavorite(System.currentTimeMillis());
        if (!isComicExist()) {
            long id = mComicDao.insert(comic);
            comic.setId(id);
            ExLog.d("ComicManager", "insert " + comic.getTitle() + " to " + comic.getId());
        }
        ExLog.d("ComicManager", "favorite" + " [" + comic.getId() + "] " + comic.getTitle());
        EventBus.getDefault().post(new EventMessage(EventMessage.FAVORITE_COMIC, new MiniComic(comic)));
    }

    public void unfavoriteComic() {
        long id = comic.getId();
        comic.setFavorite(null);
        if (!isComicHistory()) {
            mComicDao.delete(comic);
            comic.setId(null);
            ExLog.d("ComicManager", "delete" + " [" + id + "] " + comic.getTitle());
        }
        ExLog.d("ComicManager", "unfavorite" + " [" + id + "] " + comic.getTitle());
        EventBus.getDefault().post(new EventMessage(EventMessage.UN_FAVORITE_COMIC, id));
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public Comic getComic() {
        return comic;
    }

    public boolean isComicStar() {
        return comic.getFavorite() != null;
    }

    public boolean isComicHistory() {
        return comic.getHistory() != null;
    }

    public boolean isComicExist() {
        return comic.getId() != null;
    }

    public int getSource() {
        return comic.getSource();
    }

    public String getCid() {
        return comic.getCid();
    }

    public String getLast() {
        return comic.getLast();
    }

    public int getPage() {
        Integer page = comic.getPage();
        if (page == null) {
            return -1;
        }
        return page;
    }

    public void saveComic() {
        if (isComicExist()) {
            ExLog.d("ComicManager", "save" + " [" + comic.getId() + "] " + comic.getTitle());
            mComicDao.update(comic);
        }
        comic = null;
        ExLog.d("ComicManager", "clear comic");
    }

    public static ComicManager getInstance() {
        if (mComicManager == null) {
            mComicManager = new ComicManager();
        }
        return mComicManager;
    }

}
