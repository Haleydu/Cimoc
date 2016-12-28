package com.hiroshi.cimoc.core.manager;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.ComicDao.Properties;

import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;

/**
 * Created by Hiroshi on 2016/7/9.
 */
public class ComicManager {

    private static ComicManager mComicManager;

    private ComicDao mComicDao;

    private ComicManager() {
        mComicDao = CimocApplication.getDaoSession().getComicDao();
    }

    public <T> Observable<T> callInRx(Callable<T> callable) {
        return mComicDao.getSession()
                .rxTx()
                .call(callable);
    }

    public Observable<Void> runInRx(Runnable runnable) {
        return mComicDao.getSession()
                .rxTx()
                .run(runnable);
    }

    public void runInTx(Runnable runnable) {
        mComicDao.getSession().runInTx(runnable);
    }

    public <T> T callInTx(Callable<T> callable) {
        return mComicDao.getSession().callInTxNoException(callable);
    }

    public List<Comic> listFavorite() {
        return mComicDao.queryBuilder()
                .where(Properties.Favorite.isNotNull())
                .orderDesc(Properties.Highlight, Properties.Favorite)
                .list();
    }

    public List<Comic> listDownload() {
        return mComicDao.queryBuilder()
                .where(Properties.Download.isNotNull())
                .list();
    }

    public Observable<List<Comic>> listFavoriteInRx() {
        return mComicDao.queryBuilder()
                .where(Properties.Favorite.isNotNull())
                .orderDesc(Properties.Highlight, Properties.Favorite)
                .rx()
                .list();
    }

    public Observable<List<Comic>> listFinishInRx() {
        return mComicDao.queryBuilder()
                .where(Properties.Favorite.isNotNull(), Properties.Finish.eq(true))
                .orderDesc(Properties.Highlight, Properties.Favorite)
                .rx()
                .list();
    }

    public Observable<List<Comic>> listContinueInRx() {
        return mComicDao.queryBuilder()
                .where(Properties.Favorite.isNotNull(), Properties.Finish.notEq(true))
                .orderDesc(Properties.Highlight, Properties.Favorite)
                .rx()
                .list();
    }

    public Observable<List<Comic>> listHistoryInRx() {
        return mComicDao.queryBuilder()
                .where(Properties.History.isNotNull())
                .orderDesc(Properties.History)
                .rx()
                .list();
    }

    public Observable<List<Comic>> listDownloadInRx() {
        return mComicDao.queryBuilder()
                .where(Properties.Download.isNotNull())
                .orderDesc(Properties.Download)
                .rx()
                .list();
    }

    public Comic load(long id) {
        return mComicDao.load(id);
    }

    public Comic load(int source, String cid) {
        return mComicDao.queryBuilder()
                .where(Properties.Source.eq(source), Properties.Cid.eq(cid))
                .unique();
    }

    public Comic loadOrCreate(int source, String cid) {
        Comic comic = load(source, cid);
        return comic == null ? new Comic(source, cid) : comic;
    }

    public Observable<Comic> loadLast() {
        return mComicDao.queryBuilder()
                .where(Properties.History.isNotNull())
                .orderDesc(Properties.History)
                .limit(1)
                .rx()
                .unique();
    }

    public void updateOrInsert(Comic comic) {
        if (comic.getId() == null) {
            insert(comic);
        } else {
            update(comic);
        }
    }

    public void update(Comic comic) {
        mComicDao.update(comic);
    }

    public void updateOrDelete(Comic comic) {
        if (comic.getFavorite() == null && comic.getHistory() == null && comic.getDownload() == null) {
            mComicDao.delete(comic);
            comic.setId(null);
        } else {
            update(comic);
        }
    }

    public void delete(Comic comic) {
        if (comic.getFavorite() == null && comic.getHistory() == null && comic.getDownload() == null) {
            mComicDao.delete(comic);
            comic.setId(null);
        }
    }

    public void insert(Comic comic) {
        long id = mComicDao.insert(comic);
        comic.setId(id);
    }

    public static ComicManager getInstance() {
        if (mComicManager == null) {
            mComicManager = new ComicManager();
        }
        return mComicManager;
    }

}
