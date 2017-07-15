package com.hiroshi.cimoc.manager;

import com.hiroshi.cimoc.component.AppGetter;
import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.model.TagRefDao;

import java.util.List;

import rx.Observable;

/**
 * Created by Hiroshi on 2017/1/16.
 */

public class TagRefManager {

    private static TagRefManager mInstance;

    private TagRefDao mRefDao;

    private TagRefManager(AppGetter getter) {
        mRefDao = getter.getAppInstance().getDaoSession().getTagRefDao();
    }

    public Observable<Void> runInRx(Runnable runnable) {
        return mRefDao.getSession().rxTx().run(runnable);
    }

    public void runInTx(Runnable runnable) {
        mRefDao.getSession().runInTx(runnable);
    }

    public List<TagRef> listByTag(long tid) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(tid))
                .list();
    }

    public List<TagRef> listByComic(long cid) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Cid.eq(cid))
                .list();
    }

    public TagRef load(long tid, long cid) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(tid), TagRefDao.Properties.Cid.eq(cid))
                .unique();
    }

    public long insert(TagRef ref) {
        return mRefDao.insert(ref);
    }

    public void insert(Iterable<TagRef> entities) {
        mRefDao.insertInTx(entities);
    }

    public void insertInTx(Iterable<TagRef> entities) {
        mRefDao.insertInTx(entities);
    }

    public void deleteByTag(long tid) {
        mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(tid))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    public void deleteByComic(long cid) {
        mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Cid.eq(cid))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    public void delete(long tid, long cid) {
        mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(tid), TagRefDao.Properties.Cid.eq(cid))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();
    }

    public static TagRefManager getInstance(AppGetter getter) {
        if (mInstance == null) {
            synchronized (TagRefManager.class) {
                if (mInstance == null) {
                    mInstance = new TagRefManager(getter);
                }
            }
        }
        return mInstance;
    }

}
