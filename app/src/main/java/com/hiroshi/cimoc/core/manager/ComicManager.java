package com.hiroshi.cimoc.core.manager;

import android.database.Cursor;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.ComicDao.Properties;
import com.hiroshi.cimoc.model.EventMessage;
import com.hiroshi.cimoc.model.MiniComic;

import org.greenrobot.eventbus.EventBus;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Hiroshi on 2016/7/9.
 */
public class ComicManager {

    public static final long NEW_VALUE = 0xFFFFFFFFFFFL;

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
                    Comic temp = mComicDao.queryBuilder()
                            .where(Properties.Source.eq(comic.getSource()), Properties.Cid.eq(comic.getCid()))
                            .unique();
                    if (temp == null) {
                        comic.setFavorite(System.currentTimeMillis());
                        long id = mComicDao.insert(comic);
                        comic.setId(id);
                        result.add(0, new MiniComic(comic));
                    } else if (temp.getFavorite() == null) {
                        temp.setFavorite(System.currentTimeMillis());
                        mComicDao.update(temp);
                        result.add(0, new MiniComic(temp));
                    }
                }
                EventBus.getDefault().post(new EventMessage(EventMessage.RESTORE_FAVORITE, result));
            }
        });
    }

    public void updateFavorite(final List<MiniComic> list) {
        mComicDao.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                for (MiniComic comic : list) {
                    Comic temp = mComicDao.load(comic.getId());
                    if (temp != null && !comic.getUpdate().equals(temp.getUpdate())) {
                        temp.setUpdate(comic.getUpdate());
                        temp.setFavorite(NEW_VALUE);
                        mComicDao.update(temp);
                    }
                }
            }
        });
    }

    public void deleteBySource(final int source) {
        mComicDao.getSession().runInTx(new Runnable() {
            @Override
            public void run() {
                List<Comic> list = mComicDao.queryBuilder()
                        .where(Properties.Source.eq(source))
                        .list();
                for (Comic comic : list) {
                    mComicDao.delete(comic);
                }
            }
        });
    }

    public void deleteFavorite(long id) {
        Comic comic = mComicDao.load(id);
        if (comic.getHistory() == null) {
            mComicDao.delete(comic);
        } else {
            comic.setFavorite(null);
            mComicDao.update(comic);
        }
    }

    public void deleteHistory(long id) {
        Comic comic = mComicDao.load(id);
        if (comic.getFavorite() == null) {
            mComicDao.delete(comic);
        } else {
            comic.setHistory(null);
            mComicDao.update(comic);
        }
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

    public MiniComic[] arrayFavorite() {
        Cursor cursor = mComicDao.queryBuilder()
                .where(ComicDao.Properties.Favorite.isNotNull())
                .buildCursor()
                .query();
        List<MiniComic> list = listByCursor(cursor);
        return list.toArray(new MiniComic[cursor.getCount()]);
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
            boolean status = cursor.getLong(6) == NEW_VALUE;
            list.add(new MiniComic(id, source, cid, title, cover, update, status));
        }
        cursor.close();
        return list;
    }

    public Comic getComic(Long id, int source, String cid) {
        Comic comic;
        if (id == null) {
            comic = mComicDao.queryBuilder()
                    .where(Properties.Source.eq(source), Properties.Cid.eq(cid))
                    .unique();
        } else {
            comic = mComicDao.load(id);
            if (comic.getFavorite() != null && comic.getFavorite() == NEW_VALUE) {
                comic.setFavorite(System.currentTimeMillis());
            }
        }
        if (comic == null) {
            comic = new Comic(source, cid);
        }
        return comic;
    }

    public void updateComic(Comic comic) {
        mComicDao.update(comic);
    }

    public void deleteComic(long id) {
        mComicDao.deleteByKey(id);
    }

    public long insertComic(Comic comic) {
        long id = mComicDao.insert(comic);
        return id;
    }

    public static ComicManager getInstance() {
        if (mComicManager == null) {
            mComicManager = new ComicManager();
        }
        return mComicManager;
    }

}
