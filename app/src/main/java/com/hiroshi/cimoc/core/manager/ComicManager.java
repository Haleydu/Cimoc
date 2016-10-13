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

    public <T> Observable<T> callInTx(Callable<T> callable) {
        return mComicDao.getSession()
                .rxTx()
                .call(callable);
    }

    public void runInTx(Runnable runnable) {
        mComicDao.getSession().runInTx(runnable);
    }

    public Observable<List<Comic>> listSource(int source) {
        return mComicDao.queryBuilder()
                .where(Properties.Source.eq(source))
                .rx()
                .list();
    }

    public Observable<List<Comic>> listFavorite() {
        return mComicDao.queryBuilder()
                .where(Properties.Favorite.isNotNull())
                .orderDesc(Properties.Highlight, Properties.Favorite)
                .rx()
                .list();
    }

    public Observable<List<Comic>> listHistory() {
        return mComicDao.queryBuilder()
                .where(Properties.History.isNotNull())
                .orderDesc(Properties.History)
                .rx()
                .list();
    }

    public Observable<List<Comic>> listDownload() {
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

    public Observable<Comic> loadLast() {
        return mComicDao.queryBuilder()
                .orderDesc(Properties.History)
                .limit(1)
                .rx()
                .unique();
    }

    public void update(Comic comic) {
        mComicDao.update(comic);
    }

    public void delete(Comic comic) {
        mComicDao.delete(comic);
    }

    public void deleteByKey(long id) {
        mComicDao.deleteByKey(id);
    }

    public void deleteInTx(List<Comic> list) {
        mComicDao.deleteInTx(list);
    }

    public long insert(Comic comic) {
        return mComicDao.insert(comic);
    }

    public static ComicManager getInstance() {
        if (mComicManager == null) {
            mComicManager = new ComicManager();
        }
        return mComicManager;
    }

}
