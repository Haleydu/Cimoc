package com.hiroshi.cimoc.core.manager;

import com.hiroshi.cimoc.model.TagRef;
import com.hiroshi.cimoc.model.TagRefDao;
import com.hiroshi.cimoc.ui.view.BaseView;

import java.util.List;

import rx.Observable;

/**
 * Created by Hiroshi on 2017/1/16.
 */

public class TagRefManager {

    private static TagRefManager mInstance;

    private TagRefDao mRefDao;

    private TagRefManager(BaseView view) {
        mRefDao = view.getAppInstance().getDaoSession().getTagRefDao();
    }

    public Observable<Void> runInRx(Runnable runnable) {
        return mRefDao.getSession().rxTx().run(runnable);
    }

    public void runInTx(Runnable runnable) {
        mRefDao.getSession().runInTx(runnable);
    }

    public Observable<List<TagRef>> listByTagInRx(long id) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(id))
                .rx()
                .list();
    }

    public List<TagRef> listByTag(long id) {
        return mRefDao.queryBuilder()
                .where(TagRefDao.Properties.Tid.eq(id))
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
                .limit(1)
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

    public static TagRefManager getInstance(BaseView view) {
        if (mInstance == null) {
            mInstance = new TagRefManager(view);
        }
        return mInstance;
    }

}
