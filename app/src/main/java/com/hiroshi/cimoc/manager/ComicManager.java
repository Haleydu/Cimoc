package com.hiroshi.cimoc.manager;

import com.hiroshi.cimoc.component.AppGetter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ComicDao;
import com.hiroshi.cimoc.model.ComicDao.Properties;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.model.TagRefDao;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;

/**
 * Created by Hiroshi on 2016/7/9.
 */
public class ComicManager {

    private static ComicManager mInstance;

    private ComicDao mComicDao;

    private ComicManager(AppGetter getter) {
        mComicDao = getter.getAppInstance().getDaoSession().getComicDao();
    }

    public void runInTx(Runnable runnable) {
        mComicDao.getSession().runInTx(runnable);
    }

    public <T> T callInTx(Callable<T> callable) {
        return mComicDao.getSession().callInTxNoException(callable);
    }

    public List<Comic> listDownload() {
        return mComicDao.queryBuilder()
                .where(Properties.Download.isNotNull())
                .list();
    }

    public List<Comic> listLocal() {
        return mComicDao.queryBuilder()
                .where(Properties.Local.eq(true))
                .list();
    }

    public Observable<List<Comic>> listLocalInRx() {
        return mComicDao.queryBuilder()
                .where(Properties.Local.eq(true))
                .rx()
                .list();
    }

    public Observable<List<Comic>> listFavoriteOrHistoryInRx() {
        return mComicDao.queryBuilder()
                .whereOr(Properties.Favorite.isNotNull(), Properties.History.isNotNull())
                .rx()
                .list();
    }

    public List<Comic> listFavorite() {
        return mComicDao.queryBuilder()
                .where(Properties.Favorite.isNotNull())
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

    public Observable<List<Comic>> listFavoriteByTag(long id) {
        QueryBuilder<Comic> queryBuilder = mComicDao.queryBuilder();
        queryBuilder.join(TagRef.class, TagRefDao.Properties.Cid).where(TagRefDao.Properties.Tid.eq(id));
        return queryBuilder.orderDesc(Properties.Highlight, Properties.Favorite)
                .rx()
                .list();
    }

    public Observable<List<Comic>> listFavoriteNotIn(Collection<Long> collections) {
        return mComicDao.queryBuilder()
                .where(Properties.Favorite.isNotNull(), Properties.Id.notIn(collections))
                .rx()
                .list();
    }

    public long countBySource(int type) {
        return mComicDao.queryBuilder()
                .where(Properties.Source.eq(type), Properties.Favorite.isNotNull())
                .count();
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

    public void cancelHighlight() {
        mComicDao.getDatabase().execSQL("UPDATE \"COMIC\" SET \"HIGHLIGHT\" = 0 WHERE \"HIGHLIGHT\" = 1");
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

    public void deleteByKey(long key) {
        mComicDao.deleteByKey(key);
    }

    public void insert(Comic comic) {
        long id = mComicDao.insert(comic);
        comic.setId(id);
    }

    public static ComicManager getInstance(AppGetter getter) {
        if (mInstance == null) {
            synchronized (ComicManager.class) {
                if (mInstance == null) {
                    mInstance = new ComicManager(getter);
                }
            }
        }
        return mInstance;
    }

}
